package com.example.cars.auth.application.service;

import com.example.cars.auth.application.port.in.AuthResult;
import com.example.cars.auth.application.port.in.RegisterCommand;
import com.example.cars.auth.application.port.out.PasswordHasherPort;
import com.example.cars.auth.application.port.out.TokenProviderPort;
import com.example.cars.shared.domain.InvalidInputException;
import com.example.cars.users.application.port.out.UserRepositoryPort;
import com.example.cars.users.domain.Email;
import com.example.cars.users.domain.EmailAlreadyUsedException;
import com.example.cars.users.domain.User;
import com.example.cars.users.domain.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private PasswordHasherPort passwordHasher;
    @Mock
    private TokenProviderPort tokenProvider;

    private RegisterUserService service;

    @BeforeEach
    void setUp() {
        service = new RegisterUserService(userRepository, passwordHasher, tokenProvider);
    }

    @Test
    void registers_hashes_password_and_returns_a_token() {
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(passwordHasher.hash("password123")).thenReturn("HASHED");
        User saved = User.reconstitute(UserId.of(7), "Ana", Email.of("ana@test.com"), "HASHED");
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(tokenProvider.generateToken(UserId.of(7))).thenReturn("JWT");

        AuthResult result = service.register(new RegisterCommand("Ana", "ana@test.com", "password123"));

        assertThat(result.token()).isEqualTo("JWT");
        assertThat(result.userId()).isEqualTo(7);
        verify(passwordHasher).hash("password123");
    }

    @Test
    void rejects_an_already_used_email() {
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(true);

        assertThatThrownBy(() -> service.register(new RegisterCommand("Ana", "ana@test.com", "password123")))
                .isInstanceOf(EmailAlreadyUsedException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void rejects_a_password_shorter_than_eight_characters() {
        assertThatThrownBy(() -> service.register(new RegisterCommand("Ana", "ana@test.com", "short")))
                .isInstanceOf(InvalidInputException.class);
        verify(userRepository, never()).save(any());
    }
}
