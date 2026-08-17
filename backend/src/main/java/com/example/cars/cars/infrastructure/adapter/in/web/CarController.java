package com.example.cars.cars.infrastructure.adapter.in.web;

import com.example.cars.cars.application.port.in.CreateCarCommand;
import com.example.cars.cars.application.port.in.CreateCarUseCase;
import com.example.cars.cars.application.port.in.DeleteCarUseCase;
import com.example.cars.cars.application.port.in.GetCarUseCase;
import com.example.cars.cars.application.port.in.ListCarsUseCase;
import com.example.cars.cars.application.port.in.UpdateCarCommand;
import com.example.cars.cars.application.port.in.UpdateCarUseCase;
import com.example.cars.cars.infrastructure.adapter.in.web.dto.CarRequest;
import com.example.cars.cars.infrastructure.adapter.in.web.dto.CarResponse;
import com.example.cars.users.domain.UserId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** REST adapter for car management. The authenticated user id is the JWT principal. */
@RestController
@RequestMapping("/api/cars")
public class CarController {

    private final CreateCarUseCase createCar;
    private final ListCarsUseCase listCars;
    private final GetCarUseCase getCar;
    private final UpdateCarUseCase updateCar;
    private final DeleteCarUseCase deleteCar;

    public CarController(CreateCarUseCase createCar, ListCarsUseCase listCars, GetCarUseCase getCar,
                         UpdateCarUseCase updateCar, DeleteCarUseCase deleteCar) {
        this.createCar = createCar;
        this.listCars = listCars;
        this.getCar = getCar;
        this.updateCar = updateCar;
        this.deleteCar = deleteCar;
    }

    @GetMapping
    public List<CarResponse> list(@AuthenticationPrincipal Long userId) {
        return listCars.list(UserId.of(userId)).stream().map(CarResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CarResponse create(@AuthenticationPrincipal Long userId, @Valid @RequestBody CarRequest request) {
        return CarResponse.from(createCar.create(UserId.of(userId), toCreateCommand(request)));
    }

    @GetMapping("/{id}")
    public CarResponse get(@AuthenticationPrincipal Long userId, @PathVariable long id) {
        return CarResponse.from(getCar.get(UserId.of(userId), id));
    }

    @PutMapping("/{id}")
    public CarResponse update(@AuthenticationPrincipal Long userId, @PathVariable long id,
                              @Valid @RequestBody CarRequest request) {
        return CarResponse.from(updateCar.update(UserId.of(userId), id, toUpdateCommand(request)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Long userId, @PathVariable long id) {
        deleteCar.delete(UserId.of(userId), id);
    }

    private CreateCarCommand toCreateCommand(CarRequest r) {
        return new CreateCarCommand(r.brand(), r.model(), r.year(), r.plateNumber(), r.color(), r.photoUrl());
    }

    private UpdateCarCommand toUpdateCommand(CarRequest r) {
        return new UpdateCarCommand(r.brand(), r.model(), r.year(), r.plateNumber(), r.color(), r.photoUrl());
    }
}
