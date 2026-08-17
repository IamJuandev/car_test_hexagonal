package com.example.cars.auth.application.port.out;

/** Outbound port for password hashing. Implemented with BCrypt in infrastructure. */
public interface PasswordHasherPort {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String passwordHash);
}
