package com.example.cars.shared.infrastructure.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Serves uploaded photos from disk under {@code /uploads/**}.
 *
 * <p>They are deliberately not written to {@code src/main/resources/static}:
 * that folder is packaged into the jar at build time, so files added at runtime
 * would not be visible and would be lost on every rebuild. A directory outside
 * the artifact can also be mounted as a volume.
 */
@Configuration
public class StaticUploadsConfig implements WebMvcConfigurer {

    private final Path directory;

    public StaticUploadsConfig(@Value("${app.uploads.dir}") String directory) {
        this.directory = Path.of(directory).toAbsolutePath().normalize();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(locationUri())
                .setCachePeriod(3600);
    }

    /**
     * A resource location must end in a slash to be treated as a directory, and
     * {@link Path#toUri()} only adds one for a directory that already exists —
     * which it does not on a first run. Creating it up front and appending the
     * slash defensively keeps the very first upload servable.
     */
    private String locationUri() {
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create the uploads directory", e);
        }
        String uri = directory.toUri().toString();
        return uri.endsWith("/") ? uri : uri + "/";
    }
}
