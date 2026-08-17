package com.example.cars.users.application.port.out;

import com.example.cars.users.domain.Email;
import com.example.cars.users.domain.User;

import java.util.Optional;

/** Outbound port for user persistence. Implemented by an infrastructure adapter. */
public interface UserRepositoryPort {

    boolean existsByEmail(Email email);

    Optional<User> findByEmail(Email email);

    /** Persists a new user and returns it with its assigned id. */
    User save(User user);
}
