package com.example.cars.cars.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "cars")
public class CarJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;

    @Column(name = "year", nullable = false)
    private int year;

    @Column(name = "plate_number", nullable = false)
    private String plateNumber;

    @Column(nullable = false)
    private String color;

    @Column(name = "photo_url")
    private String photoUrl;

    // Defaulted by the database on insert; never written by the app.
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    protected CarJpaEntity() {
    }

    public CarJpaEntity(Long id, Long userId, String brand, String model, int year,
                        String plateNumber, String color, String photoUrl) {
        this.id = id;
        this.userId = userId;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.plateNumber = plateNumber;
        this.color = color;
        this.photoUrl = photoUrl;
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public int getYear() {
        return year;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public String getColor() {
        return color;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }
}
