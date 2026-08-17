package com.example.cars.cars.domain;

import com.example.cars.shared.domain.InvalidInputException;

/** Identity of a car. */
public record CarId(long value) {

    public CarId {
        if (value <= 0) {
            throw new InvalidInputException("CarId must be positive");
        }
    }

    public static CarId of(long value) {
        return new CarId(value);
    }
}
