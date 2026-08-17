package com.example.cars.users.infrastructure.adapter.out.persistence;

import com.example.cars.users.application.port.out.UserRepositoryPort;
import com.example.cars.users.domain.Email;
import com.example.cars.users.domain.User;
import com.example.cars.users.domain.UserId;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Adapts the JPA repository to the {@link UserRepositoryPort} domain contract. */
@Component
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final UserJpaRepository jpa;

    public UserPersistenceAdapter(UserJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public boolean existsByEmail(Email email) {
        return jpa.existsByEmail(email.value());
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return jpa.findByEmail(email.value()).map(this::toDomain);
    }

    @Override
    public User save(User user) {
        UserJpaEntity saved = jpa.save(new UserJpaEntity(
                user.id() == null ? null : user.id().value(),
                user.name(),
                user.email().value(),
                user.passwordHash()));
        return toDomain(saved);
    }

    private User toDomain(UserJpaEntity e) {
        return User.reconstitute(UserId.of(e.getId()), e.getName(), Email.of(e.getEmail()), e.getPasswordHash());
    }
}
