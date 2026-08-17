package com.example.cars.auth.application.port.in;

/** Inbound port: authenticate an existing user. */
public interface LoginUseCase {

    AuthResult login(LoginCommand command);
}
