package com.example.cars.cars.application.service;

import com.example.cars.cars.application.port.in.CreateCarCommand;
import com.example.cars.cars.application.port.in.UpdateCarCommand;
import com.example.cars.cars.application.port.out.CarRepositoryPort;
import com.example.cars.cars.domain.Car;
import com.example.cars.cars.domain.CarId;
import com.example.cars.cars.domain.CarNotFoundException;
import com.example.cars.cars.domain.DuplicatePlateException;
import com.example.cars.users.domain.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarServiceTest {

    @Mock
    private CarRepositoryPort carRepository;

    private CarService service;

    private final UserId owner = UserId.of(1);
    private final UserId otherUser = UserId.of(2);

    @BeforeEach
    void setUp() {
        service = new CarService(carRepository);
    }

    @Test
    void creates_and_saves_a_car() {
        when(carRepository.existsByOwnerAndPlate(owner, "ABC123")).thenReturn(false);
        Car persisted = Car.reconstitute(CarId.of(10), owner, "Toyota", "Corolla", 2020, "ABC123", "red", null);
        when(carRepository.save(any(Car.class))).thenReturn(persisted);

        Car result = service.create(owner,
                new CreateCarCommand("Toyota", "Corolla", 2020, "ABC123", "red", null));

        assertThat(result.id().value()).isEqualTo(10);
        verify(carRepository).save(any(Car.class));
    }

    @Test
    void rejects_creating_a_car_with_a_duplicate_plate() {
        when(carRepository.existsByOwnerAndPlate(owner, "ABC123")).thenReturn(true);

        assertThatThrownBy(() -> service.create(owner,
                new CreateCarCommand("Toyota", "Corolla", 2020, "ABC123", "red", null)))
                .isInstanceOf(DuplicatePlateException.class);
        verify(carRepository, never()).save(any());
    }

    @Test
    void returns_a_car_owned_by_the_user() {
        Car car = Car.reconstitute(CarId.of(5), owner, "Toyota", "Corolla", 2020, "ABC123", "red", null);
        when(carRepository.findById(CarId.of(5))).thenReturn(Optional.of(car));

        assertThat(service.get(owner, 5).id().value()).isEqualTo(5);
    }

    // --- Ownership rule: a user must never touch another user's car ---

    @Test
    void getting_another_users_car_is_reported_as_not_found() {
        Car someoneElses = Car.reconstitute(CarId.of(5), otherUser, "Toyota", "Corolla", 2020, "ABC123", "red", null);
        when(carRepository.findById(CarId.of(5))).thenReturn(Optional.of(someoneElses));

        assertThatThrownBy(() -> service.get(owner, 5)).isInstanceOf(CarNotFoundException.class);
    }

    @Test
    void updating_another_users_car_is_rejected_and_never_persisted() {
        Car someoneElses = Car.reconstitute(CarId.of(5), otherUser, "Toyota", "Corolla", 2020, "ABC123", "red", null);
        when(carRepository.findById(CarId.of(5))).thenReturn(Optional.of(someoneElses));

        assertThatThrownBy(() -> service.update(owner, 5,
                new UpdateCarCommand("Honda", "Civic", 2021, "NEW999", "black", null)))
                .isInstanceOf(CarNotFoundException.class);
        verify(carRepository, never()).save(any());
    }

    @Test
    void deleting_another_users_car_is_rejected_and_never_deleted() {
        Car someoneElses = Car.reconstitute(CarId.of(5), otherUser, "Toyota", "Corolla", 2020, "ABC123", "red", null);
        when(carRepository.findById(CarId.of(5))).thenReturn(Optional.of(someoneElses));

        assertThatThrownBy(() -> service.delete(owner, 5)).isInstanceOf(CarNotFoundException.class);
        verify(carRepository, never()).deleteById(any());
    }

    @Test
    void getting_a_missing_car_is_not_found() {
        when(carRepository.findById(CarId.of(99))).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.get(owner, 99)).isInstanceOf(CarNotFoundException.class);
    }

    @Test
    void lists_only_the_owners_cars() {
        Car car = Car.reconstitute(CarId.of(1), owner, "Toyota", "Corolla", 2020, "ABC123", "red", null);
        when(carRepository.findAllByOwner(owner)).thenReturn(List.of(car));

        assertThat(service.list(owner)).hasSize(1);
        verify(carRepository).findAllByOwner(owner);
    }

    @Test
    void updates_an_owned_car() {
        Car car = Car.reconstitute(CarId.of(5), owner, "Toyota", "Corolla", 2020, "ABC123", "red", null);
        when(carRepository.findById(CarId.of(5))).thenReturn(Optional.of(car));
        when(carRepository.save(any(Car.class))).thenAnswer(inv -> inv.getArgument(0));

        Car updated = service.update(owner, 5,
                new UpdateCarCommand("Honda", "Civic", 2021, "ABC123", "black", null));

        assertThat(updated.model()).isEqualTo("Civic");
        verify(carRepository).save(any(Car.class));
    }
}
