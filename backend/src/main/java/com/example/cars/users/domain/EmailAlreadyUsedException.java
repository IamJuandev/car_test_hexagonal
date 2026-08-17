package com.example.cars.users.domain;

import com.example.cars.shared.domain.DomainException;

/** Raised when registering with an email that already exists. Maps to HTTP 409. */
public class EmailAlreadyUsedException extends DomainException {

    public EmailAlreadyUsedException(String email) {
        super("Email already in use: " + email);
    }
}
