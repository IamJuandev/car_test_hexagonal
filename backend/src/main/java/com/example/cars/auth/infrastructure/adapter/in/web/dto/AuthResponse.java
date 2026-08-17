package com.example.cars.auth.infrastructure.adapter.in.web.dto;

import com.example.cars.auth.application.port.in.AuthResult;

public record AuthResponse(String token, long userId, String name, String email) {

    public static AuthResponse from(AuthResult result) {
        return new AuthResponse(result.token(), result.userId(), result.name(), result.email());
    }
}
