package com.example.cars.cars.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CarRequest(
        @NotBlank String brand,
        @NotBlank String model,
        @Min(value = 1886, message = "Year must be 1886 or later") int year,
        @NotBlank String plateNumber,
        @NotBlank String color,
        String photoUrl) {
}
