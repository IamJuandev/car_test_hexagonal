package com.example.cars.cars.infrastructure.adapter.out.persistence;

import com.example.cars.cars.application.port.out.CarRepositoryPort;
import com.example.cars.cars.domain.Car;
import com.example.cars.cars.domain.CarId;
import com.example.cars.users.domain.UserId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Adapts the JPA repository to the {@link CarRepositoryPort} domain contract. */
@Component
public class CarPersistenceAdapter implements CarRepositoryPort {

    private final CarJpaRepository jpa;

    public CarPersistenceAdapter(CarJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Car save(Car car) {
        CarJpaEntity saved = jpa.save(new CarJpaEntity(
                car.id() == null ? null : car.id().value(),
                car.ownerId().value(),
                car.brand(), car.model(), car.year(),
                car.plateNumber(), car.color(), car.photoUrl()));
        return toDomain(saved);
    }

    @Override
    public Optional<Car> findById(CarId carId) {
        return jpa.findById(carId.value()).map(this::toDomain);
    }

    @Override
    public List<Car> findAllByOwner(UserId owner) {
        return jpa.findAllByUserId(owner.value()).stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(CarId carId) {
        jpa.deleteById(carId.value());
    }

    @Override
    public boolean existsByOwnerAndPlate(UserId owner, String plateNumber) {
        return jpa.existsByUserIdAndPlateNumber(owner.value(), plateNumber);
    }

    private Car toDomain(CarJpaEntity e) {
        return Car.reconstitute(CarId.of(e.getId()), UserId.of(e.getUserId()),
                e.getBrand(), e.getModel(), e.getYear(),
                e.getPlateNumber(), e.getColor(), e.getPhotoUrl());
    }
}
