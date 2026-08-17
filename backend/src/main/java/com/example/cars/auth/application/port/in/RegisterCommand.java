package com.example.cars.auth.application.port.in;

public record RegisterCommand(String name, String email, String password) {
}
