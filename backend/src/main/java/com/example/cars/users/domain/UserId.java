package com.example.cars.users.domain;

import com.example.cars.shared.domain.InvalidInputException;

/** Identity of a user. */
public record UserId(long value) {

    public UserId {
        if (value <= 0) {
            throw new InvalidInputException("UserId must be positive");
        }
    }

    public static UserId of(long value) {
        return new UserId(value);
    }
}
