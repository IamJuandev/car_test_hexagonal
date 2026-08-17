package com.example.cars.auth.application.service;

import com.example.cars.auth.application.port.in.AuthResult;
import com.example.cars.auth.application.port.in.LoginCommand;
import com.example.cars.auth.application.port.in.LoginUseCase;
import com.example.cars.auth.application.port.out.PasswordHasherPort;
import com.example.cars.auth.application.port.out.TokenProviderPort;
import com.example.cars.users.application.port.out.UserRepositoryPort;
import com.example.cars.users.domain.Email;
import com.example.cars.users.domain.InvalidCredentialsException;
import com.example.cars.users.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginService implements LoginUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;
    private final TokenProviderPort tokenProvider;

    public LoginService(UserRepositoryPort userRepository,
                        PasswordHasherPort passwordHasher,
                        TokenProviderPort tokenProvider) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResult login(LoginCommand command) {
        Email email = Email.of(command.email());
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordHasher.matches(command.password(), user.passwordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = tokenProvider.generateToken(user.id());
        return new AuthResult(token, user.id().value(), user.name(), user.email().value());
    }
}
