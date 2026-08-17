package com.example.cars.auth.application.service;

import com.example.cars.auth.application.port.in.AuthResult;
import com.example.cars.auth.application.port.in.LoginCommand;
import com.example.cars.auth.application.port.out.PasswordHasherPort;
import com.example.cars.auth.application.port.out.TokenProviderPort;
import com.example.cars.users.application.port.out.UserRepositoryPort;
import com.example.cars.users.domain.Email;
import com.example.cars.users.domain.InvalidCredentialsException;
import com.example.cars.users.domain.User;
import com.example.cars.users.domain.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private PasswordHasherPort passwordHasher;
    @Mock
    private TokenProviderPort tokenProvider;

    private LoginService service;

    @BeforeEach
    void setUp() {
        service = new LoginService(userRepository, passwordHasher, tokenProvider);
    }

    private User existingUser() {
        return User.reconstitute(UserId.of(3), "Bob", Email.of("bob@test.com"), "HASHED");
    }

    @Test
    void logs_in_with_valid_credentials_and_returns_a_token() {
        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(existingUser()));
        when(passwordHasher.matches("secret123", "HASHED")).thenReturn(true);
        when(tokenProvider.generateToken(UserId.of(3))).thenReturn("JWT");

        AuthResult result = service.login(new LoginCommand("bob@test.com", "secret123"));

        assertThat(result.token()).isEqualTo("JWT");
        assertThat(result.userId()).isEqualTo(3);
    }

    @Test
    void rejects_a_wrong_password() {
        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(existingUser()));
        when(passwordHasher.matches("wrong", "HASHED")).thenReturn(false);

        assertThatThrownBy(() -> service.login(new LoginCommand("bob@test.com", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void rejects_an_unknown_email() {
        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new LoginCommand("ghost@test.com", "whatever")))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
