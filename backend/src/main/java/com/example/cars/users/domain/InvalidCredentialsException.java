package com.example.cars.users.domain;

import com.example.cars.shared.domain.DomainException;

/** Raised when login credentials do not match. Maps to HTTP 401. */
public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
