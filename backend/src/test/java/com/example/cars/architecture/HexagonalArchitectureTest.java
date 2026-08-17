package com.example.cars.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import java.util.Set;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Executable guard for the hexagonal boundaries. Folder names alone do not keep
 * an architecture honest: these rules fail the build when a dependency points
 * the wrong way.
 *
 * <p>The dependency rule is always inward: infrastructure -> application -> domain.
 * The domain knows nothing about the outside world; the application layer knows
 * only its own ports.
 */
@AnalyzeClasses(packages = "com.example.cars", importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {

    private static final String[] FRAMEWORK_PACKAGES = {
            "org.springframework..", "jakarta..", "org.hibernate..", "com.fasterxml.jackson.."
    };

    /**
     * The concession this codebase deliberately makes: application services are
     * declared with Spring's stereotype and transaction annotations instead of
     * being wired by an infrastructure configuration class. Everything else from
     * the framework stays out of the application layer.
     */
    private static final Set<String> ALLOWED_FRAMEWORK_TYPES = Set.of(
            "org.springframework.stereotype.Service",
            "org.springframework.transaction.annotation.Transactional",
            // @Transactional carries these two enums as annotation members, so a
            // class using it necessarily references them too.
            "org.springframework.transaction.annotation.Isolation",
            "org.springframework.transaction.annotation.Propagation");

    private static final DescribedPredicate<JavaClass> FRAMEWORK_TYPES_BEYOND_THE_ALLOWED_ANNOTATIONS =
            new DescribedPredicate<>("framework types other than @Service and @Transactional") {
                @Override
                public boolean test(JavaClass javaClass) {
                    String name = javaClass.getName();
                    boolean isFramework = name.startsWith("org.springframework.")
                            || name.startsWith("jakarta.")
                            || name.startsWith("org.hibernate.")
                            || name.startsWith("com.fasterxml.jackson.");
                    return isFramework && !ALLOWED_FRAMEWORK_TYPES.contains(name);
                }
            };

    // --- The domain is the centre: it depends on nothing but itself ---

    @ArchTest
    static final ArchRule domain_is_free_of_frameworks = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(FRAMEWORK_PACKAGES)
            .because("domain entities must be plain Java, testable without Spring, JPA or a database");

    @ArchTest
    static final ArchRule domain_does_not_depend_on_outer_layers = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage("..application..", "..infrastructure..")
            .because("the dependency rule points inward: the centre never reaches out");

    // --- The application layer orchestrates through ports only ---

    @ArchTest
    static final ArchRule application_does_not_depend_on_infrastructure = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
            .because("use cases must not know which adapter fulfils their ports");

    @ArchTest
    static final ArchRule application_uses_only_the_two_allowed_framework_annotations = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat(FRAMEWORK_TYPES_BEYOND_THE_ALLOWED_ANNOTATIONS)
            .because("the framework concession in the application layer is limited and documented");

    @ArchTest
    static final ArchRule outbound_ports_are_interfaces = classes()
            .that().resideInAPackage("..application.port.out..")
            .should().beInterfaces()
            .because("an outbound port is a contract the infrastructure implements");

    // --- Adapters sit on the outside and talk to ports, never to services ---

    @ArchTest
    static final ArchRule inbound_adapters_depend_on_ports_not_on_services = noClasses()
            .that().resideInAPackage("..infrastructure.adapter.in..")
            .should().dependOnClassesThat().resideInAPackage("..application.service..")
            .because("controllers must be wired to the use case interfaces, not to their implementations");

    @ArchTest
    static final ArchRule rest_controllers_live_in_the_inbound_web_adapter = classes()
            .that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
            .should().resideInAPackage("..infrastructure.adapter.in.web..")
            .because("HTTP is a delivery detail and belongs at the edge");

    @ArchTest
    static final ArchRule jpa_entities_live_in_the_outbound_persistence_adapter = classes()
            .that().areAnnotatedWith("jakarta.persistence.Entity")
            .should().resideInAPackage("..infrastructure.adapter.out.persistence..")
            .because("the persistence model is a detail, kept separate from the domain model");

    // --- Business modules stay decoupled from each other ---

    /*
     * Module independence is stated as explicit one-way rules instead of a cycle
     * check over slices: `shared` is both the kernel every module builds on
     * (shared.domain) and the web edge that translates their exceptions
     * (shared.infrastructure.web), so treating it as a single slice would report a
     * cycle that does not exist in the design.
     *
     * The intended direction is: auth -> users, cars -> users, everything -> shared.
     */

    @ArchTest
    static final ArchRule users_is_independent_of_the_other_modules = noClasses()
            .that().resideInAPackage("com.example.cars.users..")
            .should().dependOnClassesThat().resideInAnyPackage("com.example.cars.auth..", "com.example.cars.cars..")
            .because("users is the shared identity kernel and must not know its consumers");

    @ArchTest
    static final ArchRule cars_does_not_depend_on_auth = noClasses()
            .that().resideInAPackage("com.example.cars.cars..")
            .should().dependOnClassesThat().resideInAPackage("com.example.cars.auth..")
            .because("car management receives an already authenticated user id, it does not authenticate");

    @ArchTest
    static final ArchRule auth_does_not_depend_on_cars = noClasses()
            .that().resideInAPackage("com.example.cars.auth..")
            .should().dependOnClassesThat().resideInAPackage("com.example.cars.cars..")
            .because("authentication knows nothing about cars");

    @ArchTest
    static final ArchRule shared_domain_is_a_kernel_that_depends_on_nobody = noClasses()
            .that().resideInAPackage("com.example.cars.shared.domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "com.example.cars.auth..", "com.example.cars.cars..", "com.example.cars.users..")
            .because("the shared kernel is depended upon, it never depends back");
}
