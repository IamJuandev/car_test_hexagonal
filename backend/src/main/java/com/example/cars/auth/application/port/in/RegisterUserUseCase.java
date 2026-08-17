package com.example.cars.auth.application.port.in;

/** Inbound port: register a new user and return an authenticated session. */
public interface RegisterUserUseCase {

    AuthResult register(RegisterCommand command);
}
