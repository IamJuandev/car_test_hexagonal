package com.example.cars.shared.domain;

/**
 * Base type for business errors raised by the domain and application layers.
 * Infrastructure (the web layer) decides how to translate these into HTTP.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }
}
