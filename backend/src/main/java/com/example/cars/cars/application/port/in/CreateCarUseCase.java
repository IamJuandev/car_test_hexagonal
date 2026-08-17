package com.example.cars.cars.application.port.in;

import com.example.cars.cars.domain.Car;
import com.example.cars.users.domain.UserId;

public interface CreateCarUseCase {

    Car create(UserId owner, CreateCarCommand command);
}
