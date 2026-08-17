package com.example.cars.users.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {

    boolean existsByEmail(String email);

    Optional<UserJpaEntity> findByEmail(String email);
}
