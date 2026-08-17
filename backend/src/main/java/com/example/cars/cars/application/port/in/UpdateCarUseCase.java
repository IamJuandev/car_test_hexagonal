package com.example.cars.cars.application.port.in;

import com.example.cars.cars.domain.Car;
import com.example.cars.users.domain.UserId;

public interface UpdateCarUseCase {

    Car update(UserId owner, long carId, UpdateCarCommand command);
}
