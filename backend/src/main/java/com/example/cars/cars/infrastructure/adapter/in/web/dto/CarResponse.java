package com.example.cars.cars.infrastructure.adapter.in.web.dto;

import com.example.cars.cars.domain.Car;

public record CarResponse(long id, String brand, String model, int year,
                          String plateNumber, String color, String photoUrl) {

    public static CarResponse from(Car car) {
        return new CarResponse(car.id().value(), car.brand(), car.model(), car.year(),
                car.plateNumber(), car.color(), car.photoUrl());
    }
}
