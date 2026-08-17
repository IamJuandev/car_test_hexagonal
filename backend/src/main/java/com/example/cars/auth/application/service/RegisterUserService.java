package com.example.cars.auth.application.service;

import com.example.cars.auth.application.port.in.AuthResult;
import com.example.cars.auth.application.port.in.RegisterCommand;
import com.example.cars.auth.application.port.in.RegisterUserUseCase;
import com.example.cars.auth.application.port.out.PasswordHasherPort;
import com.example.cars.auth.application.port.out.TokenProviderPort;
import com.example.cars.shared.domain.InvalidInputException;
import com.example.cars.users.application.port.out.UserRepositoryPort;
import com.example.cars.users.domain.Email;
import com.example.cars.users.domain.EmailAlreadyUsedException;
import com.example.cars.users.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterUserService implements RegisterUserUseCase {

    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;
    private final TokenProviderPort tokenProvider;

    public RegisterUserService(UserRepositoryPort userRepository,
                               PasswordHasherPort passwordHasher,
                               TokenProviderPort tokenProvider) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
    }

    @Override
    @Transactional
    public AuthResult register(RegisterCommand command) {
        Email email = Email.of(command.email());
        if (command.password() == null || command.password().length() < MIN_PASSWORD_LENGTH) {
            throw new InvalidInputException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyUsedException(email.value());
        }

        String hash = passwordHasher.hash(command.password());
        User saved = userRepository.save(User.register(command.name(), email, hash));

        String token = tokenProvider.generateToken(saved.id());
        return new AuthResult(token, saved.id().value(), saved.name(), saved.email().value());
    }
}
