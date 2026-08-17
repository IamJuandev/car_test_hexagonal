package com.example.cars.shared.domain;

/** Raised when domain input fails a business validation rule. Maps to HTTP 400. */
public class InvalidInputException extends DomainException {

    public InvalidInputException(String message) {
        super(message);
    }
}
