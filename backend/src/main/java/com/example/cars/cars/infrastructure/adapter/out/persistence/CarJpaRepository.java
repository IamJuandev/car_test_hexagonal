package com.example.cars.cars.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarJpaRepository extends JpaRepository<CarJpaEntity, Long> {

    List<CarJpaEntity> findAllByUserId(Long userId);

    boolean existsByUserIdAndPlateNumber(Long userId, String plateNumber);
}
