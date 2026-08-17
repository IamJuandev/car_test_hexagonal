package com.example.cars.cars.domain;

import com.example.cars.shared.domain.DomainException;

/** Raised when a user already owns a car with the same plate. Maps to HTTP 409. */
public class DuplicatePlateException extends DomainException {

    public DuplicatePlateException(String plateNumber) {
        super("A car with this plate already exists: " + plateNumber);
    }
}
