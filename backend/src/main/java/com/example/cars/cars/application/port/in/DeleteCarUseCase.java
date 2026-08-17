package com.example.cars.cars.application.port.in;

import com.example.cars.users.domain.UserId;

public interface DeleteCarUseCase {

    void delete(UserId owner, long carId);
}
