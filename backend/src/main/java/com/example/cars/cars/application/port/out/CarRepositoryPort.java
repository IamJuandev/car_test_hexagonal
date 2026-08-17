package com.example.cars.cars.application.port.out;

import com.example.cars.cars.domain.Car;
import com.example.cars.cars.domain.CarId;
import com.example.cars.users.domain.UserId;

import java.util.List;
import java.util.Optional;

/** Outbound port for car persistence. Implemented by an infrastructure adapter. */
public interface CarRepositoryPort {

    Car save(Car car);

    Optional<Car> findById(CarId carId);

    List<Car> findAllByOwner(UserId owner);

    void deleteById(CarId carId);

    boolean existsByOwnerAndPlate(UserId owner, String plateNumber);
}
