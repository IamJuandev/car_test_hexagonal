package com.example.cars.cars.application.port.in;

import com.example.cars.cars.domain.Car;
import com.example.cars.users.domain.UserId;

public interface GetCarUseCase {

    Car get(UserId owner, long carId);
}
