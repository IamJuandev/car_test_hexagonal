package com.example.cars.shared.infrastructure.web;

import java.time.Instant;
import java.util.List;

/** Uniform error payload returned by the API. */
public record ApiError(Instant timestamp, int status, String error, List<String> messages) {

    public static ApiError of(int status, String error, List<String> messages) {
        return new ApiError(Instant.now(), status, error, messages);
    }
}
