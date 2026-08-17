package com.example.cars.auth.application.port.in;

/** Outcome of a successful authentication: a JWT plus basic user identity. */
public record AuthResult(String token, long userId, String name, String email) {
}
