package com.example.cars.users.domain;

import com.example.cars.shared.domain.InvalidInputException;

import java.util.regex.Pattern;

/** Validated, normalized email address. */
public record Email(String value) {

    private static final Pattern PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public Email {
        if (value == null || value.isBlank()) {
            throw new InvalidInputException("Email is required");
        }
        value = value.trim().toLowerCase();
        if (!PATTERN.matcher(value).matches()) {
            throw new InvalidInputException("Email format is invalid");
        }
    }

    public static Email of(String value) {
        return new Email(value);
    }
}
