package com.example.cars.cars.application.port.in;

import com.example.cars.cars.domain.Car;
import com.example.cars.users.domain.UserId;

import java.util.List;

public interface ListCarsUseCase {

    List<Car> list(UserId owner);
}
