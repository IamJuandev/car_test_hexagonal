package com.example.cars.cars.application.service;

import com.example.cars.cars.application.port.in.CreateCarCommand;
import com.example.cars.cars.application.port.in.CreateCarUseCase;
import com.example.cars.cars.application.port.in.DeleteCarUseCase;
import com.example.cars.cars.application.port.in.GetCarUseCase;
import com.example.cars.cars.application.port.in.ListCarsUseCase;
import com.example.cars.cars.application.port.in.UpdateCarCommand;
import com.example.cars.cars.application.port.in.UpdateCarUseCase;
import com.example.cars.cars.application.port.out.CarRepositoryPort;
import com.example.cars.cars.domain.Car;
import com.example.cars.cars.domain.CarId;
import com.example.cars.cars.domain.CarNotFoundException;
import com.example.cars.cars.domain.DuplicatePlateException;
import com.example.cars.users.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Application service for the car CRUD use cases. This is where the ownership
 * rule is enforced: every read/edit/delete loads the car and checks it belongs
 * to the requesting user before doing anything.
 */
@Service
public class CarService implements CreateCarUseCase, ListCarsUseCase, GetCarUseCase,
        UpdateCarUseCase, DeleteCarUseCase {

    private final CarRepositoryPort carRepository;

    public CarService(CarRepositoryPort carRepository) {
        this.carRepository = carRepository;
    }

    @Override
    @Transactional
    public Car create(UserId owner, CreateCarCommand command) {
        if (carRepository.existsByOwnerAndPlate(owner, command.plateNumber())) {
            throw new DuplicatePlateException(command.plateNumber());
        }
        Car car = Car.create(owner, command.brand(), command.model(), command.year(),
                command.plateNumber(), command.color(), command.photoUrl());
        return carRepository.save(car);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Car> list(UserId owner) {
        return carRepository.findAllByOwner(owner);
    }

    @Override
    @Transactional(readOnly = true)
    public Car get(UserId owner, long carId) {
        return requireOwnedCar(owner, carId);
    }

    @Override
    @Transactional
    public Car update(UserId owner, long carId, UpdateCarCommand command) {
        Car car = requireOwnedCar(owner, carId);

        boolean plateChanged = !car.plateNumber().equals(command.plateNumber());
        if (plateChanged && carRepository.existsByOwnerAndPlate(owner, command.plateNumber())) {
            throw new DuplicatePlateException(command.plateNumber());
        }

        car.update(command.brand(), command.model(), command.year(),
                command.plateNumber(), command.color(), command.photoUrl());
        return carRepository.save(car);
    }

    @Override
    @Transactional
    public void delete(UserId owner, long carId) {
        Car car = requireOwnedCar(owner, carId);
        carRepository.deleteById(car.id());
    }

    /**
     * Loads a car and guarantees it belongs to the user. A car owned by someone
     * else is reported as "not found" so the API never leaks its existence.
     */
    private Car requireOwnedCar(UserId owner, long carId) {
        Car car = carRepository.findById(CarId.of(carId))
                .orElseThrow(() -> new CarNotFoundException(carId));
        if (!car.belongsTo(owner)) {
            throw new CarNotFoundException(carId);
        }
        return car;
    }
}
