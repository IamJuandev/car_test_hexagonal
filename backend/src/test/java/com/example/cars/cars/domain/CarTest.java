package com.example.cars.cars.domain;

import com.example.cars.shared.domain.InvalidInputException;
import com.example.cars.users.domain.UserId;
import org.junit.jupiter.api.Test;

import java.time.Year;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CarTest {

    private final UserId owner = UserId.of(1);

    @Test
    void creates_a_valid_car() {
        Car car = Car.create(owner, "Toyota", "Corolla", 2020, "ABC123", "red", null);

        assertThat(car.brand()).isEqualTo("Toyota");
        assertThat(car.ownerId()).isEqualTo(owner);
        assertThat(car.photoUrl()).isNull();
    }

    @Test
    void trims_text_and_normalizes_blank_photo_to_null() {
        Car car = Car.create(owner, "  Ford ", "Focus", 2019, "XYZ", "blue", "   ");
        assertThat(car.brand()).isEqualTo("Ford");
        assertThat(car.photoUrl()).isNull();
    }

    @Test
    void rejects_blank_brand() {
        assertThatThrownBy(() -> Car.create(owner, " ", "Corolla", 2020, "ABC123", "red", null))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void rejects_year_in_the_future() {
        int tooFar = Year.now().getValue() + 5;
        assertThatThrownBy(() -> Car.create(owner, "Toyota", "Corolla", tooFar, "ABC123", "red", null))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void belongs_to_its_owner_only() {
        Car car = Car.create(owner, "Toyota", "Corolla", 2020, "ABC123", "red", null);
        assertThat(car.belongsTo(owner)).isTrue();
        assertThat(car.belongsTo(UserId.of(2))).isFalse();
    }

    @Test
    void update_replaces_details() {
        Car car = Car.create(owner, "Toyota", "Corolla", 2020, "ABC123", "red", null);
        car.update("Honda", "Civic", 2021, "NEW999", "black", "http://img");
        assertThat(car.model()).isEqualTo("Civic");
        assertThat(car.plateNumber()).isEqualTo("NEW999");
        assertThat(car.photoUrl()).isEqualTo("http://img");
    }
}
