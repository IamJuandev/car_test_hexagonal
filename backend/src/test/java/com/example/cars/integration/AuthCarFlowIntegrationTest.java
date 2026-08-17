package com.example.cars.integration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end integration test of the auth + car CRUD flow against a REAL SQL Server.
 *
 * <p>This is NOT a Testcontainers test: the project runs the database with Apple
 * {@code container} (no Docker daemon, which Testcontainers requires). It targets the
 * live local SQL Server brought up by {@code ./scripts/container-up.sh}, exercising the
 * real {@code database/init.sql} schema instead of a Hibernate-generated one.
 *
 * <p>Tagged {@code integration} so it is excluded from {@code mvn test} (unit tests only).
 * Run it explicitly with the stack up:
 * <pre>mvn test -Dgroups=integration</pre>
 *
 * <p>Each run uses unique emails so it stays idempotent against the persistent database.
 */
@Tag("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthCarFlowIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate rest;

    @Test
    void full_flow_register_login_crud_and_ownership() {
        long stamp = System.nanoTime();
        String emailA = "it-a-" + stamp + "@test.com";
        String emailB = "it-b-" + stamp + "@test.com";
        String password = "password123";

        // --- register two users; register returns a token (201) ---
        String tokenA = register("Owner A", emailA, password);
        String tokenB = register("Owner B", emailB, password);

        // --- login also returns a token (200) ---
        ResponseEntity<Map> login = rest.postForEntity(
                url("/api/auth/login"),
                new HttpEntity<>(Map.of("email", emailA, "password", password), jsonHeaders(null)),
                Map.class);
        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(login.getBody()).containsKey("token");

        // --- no token -> 401 ---
        ResponseEntity<String> unauthorized = rest.exchange(
                url("/api/cars"), HttpMethod.GET, new HttpEntity<>(jsonHeaders(null)), String.class);
        assertThat(unauthorized.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // --- A creates a car (201) ---
        Map<String, Object> car = Map.of(
                "brand", "Toyota", "model", "Corolla", "year", 2021,
                "plateNumber", "IT-" + stamp, "color", "Silver");
        ResponseEntity<Map> created = rest.postForEntity(
                url("/api/cars"), new HttpEntity<>(car, jsonHeaders(tokenA)), Map.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        long carId = ((Number) created.getBody().get("id")).longValue();

        // --- A lists and finds it (200) ---
        ResponseEntity<List> list = rest.exchange(
                url("/api/cars"), HttpMethod.GET, new HttpEntity<>(jsonHeaders(tokenA)), List.class);
        assertThat(list.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(list.getBody()).isNotEmpty();

        // --- A reads it by id (200) ---
        ResponseEntity<Map> getOwn = rest.exchange(
                url("/api/cars/" + carId), HttpMethod.GET, new HttpEntity<>(jsonHeaders(tokenA)), Map.class);
        assertThat(getOwn.getStatusCode()).isEqualTo(HttpStatus.OK);

        // --- ownership: B cannot see or delete A's car -> 404 (does not leak existence) ---
        ResponseEntity<String> getOther = rest.exchange(
                url("/api/cars/" + carId), HttpMethod.GET, new HttpEntity<>(jsonHeaders(tokenB)), String.class);
        assertThat(getOther.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<String> deleteOther = rest.exchange(
                url("/api/cars/" + carId), HttpMethod.DELETE, new HttpEntity<>(jsonHeaders(tokenB)), String.class);
        assertThat(deleteOther.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // --- duplicate plate for the same owner -> 409 ---
        ResponseEntity<String> duplicate = rest.postForEntity(
                url("/api/cars"), new HttpEntity<>(car, jsonHeaders(tokenA)), String.class);
        assertThat(duplicate.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        // --- invalid year -> 400 ---
        Map<String, Object> badYear = Map.of(
                "brand", "Toyota", "model", "Corolla", "year", 1800,
                "plateNumber", "BAD-" + stamp, "color", "Silver");
        ResponseEntity<String> invalid = rest.postForEntity(
                url("/api/cars"), new HttpEntity<>(badYear, jsonHeaders(tokenA)), String.class);
        assertThat(invalid.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // --- A updates the car (200) ---
        Map<String, Object> update = Map.of(
                "brand", "Toyota", "model", "Corolla Cross", "year", 2022,
                "plateNumber", "IT-" + stamp, "color", "Black");
        ResponseEntity<Map> updated = rest.exchange(
                url("/api/cars/" + carId), HttpMethod.PUT, new HttpEntity<>(update, jsonHeaders(tokenA)), Map.class);
        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updated.getBody().get("model")).isEqualTo("Corolla Cross");

        // --- A deletes the car (204) ---
        ResponseEntity<Void> deleted = rest.exchange(
                url("/api/cars/" + carId), HttpMethod.DELETE, new HttpEntity<>(jsonHeaders(tokenA)), Void.class);
        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    private String register(String name, String email, String password) {
        ResponseEntity<Map> response = rest.postForEntity(
                url("/api/auth/register"),
                new HttpEntity<>(Map.of("name", name, "email", email, "password", password), jsonHeaders(null)),
                Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return (String) response.getBody().get("token");
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private HttpHeaders jsonHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.setBearerAuth(token);
        }
        return headers;
    }
}
