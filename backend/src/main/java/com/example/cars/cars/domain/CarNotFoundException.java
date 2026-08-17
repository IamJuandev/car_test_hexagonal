package com.example.cars.cars.domain;

import com.example.cars.shared.domain.DomainException;

/**
 * Raised when a car does not exist OR is not owned by the requesting user.
 * Both cases map to HTTP 404 so the API never leaks the existence of another
 * user's car.
 */
public class CarNotFoundException extends DomainException {

    public CarNotFoundException(long carId) {
        super("Car not found: " + carId);
    }
}
