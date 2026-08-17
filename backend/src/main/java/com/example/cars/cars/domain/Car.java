package com.example.cars.cars.domain;

import com.example.cars.shared.domain.InvalidInputException;
import com.example.cars.users.domain.UserId;

import java.time.Year;

/**
 * A car owned by a user. The ownership rule lives here: a car always knows who
 * it belongs to, and {@link #belongsTo(UserId)} is how use cases enforce that a
 * user only ever touches their own cars.
 */
public class Car {

    private static final int FIRST_CAR_YEAR = 1886;

    private final CarId id; // null until persisted
    private final UserId ownerId;
    private String brand;
    private String model;
    private int year;
    private String plateNumber;
    private String color;
    private String photoUrl; // optional

    private Car(CarId id, UserId ownerId, String brand, String model, int year,
                String plateNumber, String color, String photoUrl) {
        if (ownerId == null) {
            throw new InvalidInputException("A car requires an owner");
        }
        this.id = id;
        this.ownerId = ownerId;
        applyDetails(brand, model, year, plateNumber, color, photoUrl);
    }

    public static Car create(UserId ownerId, String brand, String model, int year,
                             String plateNumber, String color, String photoUrl) {
        return new Car(null, ownerId, brand, model, year, plateNumber, color, photoUrl);
    }

    public static Car reconstitute(CarId id, UserId ownerId, String brand, String model, int year,
                                   String plateNumber, String color, String photoUrl) {
        if (id == null) {
            throw new InvalidInputException("Persisted car requires an id");
        }
        return new Car(id, ownerId, brand, model, year, plateNumber, color, photoUrl);
    }

    /** Apply edited details, re-validating the invariants. */
    public void update(String brand, String model, int year, String plateNumber, String color, String photoUrl) {
        applyDetails(brand, model, year, plateNumber, color, photoUrl);
    }

    public boolean belongsTo(UserId userId) {
        return ownerId.equals(userId);
    }

    private void applyDetails(String brand, String model, int year,
                              String plateNumber, String color, String photoUrl) {
        this.brand = requireText(brand, "Brand");
        this.model = requireText(model, "Model");
        this.plateNumber = requireText(plateNumber, "Plate number");
        this.color = requireText(color, "Color");
        int maxYear = Year.now().getValue() + 1;
        if (year < FIRST_CAR_YEAR || year > maxYear) {
            throw new InvalidInputException("Year must be between " + FIRST_CAR_YEAR + " and " + maxYear);
        }
        this.year = year;
        this.photoUrl = (photoUrl == null || photoUrl.isBlank()) ? null : photoUrl.trim();
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new InvalidInputException(field + " is required");
        }
        return value.trim();
    }

    public CarId id() {
        return id;
    }

    public UserId ownerId() {
        return ownerId;
    }

    public String brand() {
        return brand;
    }

    public String model() {
        return model;
    }

    public int year() {
        return year;
    }

    public String plateNumber() {
        return plateNumber;
    }

    public String color() {
        return color;
    }

    public String photoUrl() {
        return photoUrl;
    }
}
