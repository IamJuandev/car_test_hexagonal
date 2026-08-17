package com.example.cars.cars.application.port.in;

public record UpdateCarCommand(String brand, String model, int year,
                               String plateNumber, String color, String photoUrl) {
}
