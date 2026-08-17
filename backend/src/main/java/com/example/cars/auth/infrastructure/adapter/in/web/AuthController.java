package com.example.cars.auth.infrastructure.adapter.in.web;

import com.example.cars.auth.application.port.in.LoginCommand;
import com.example.cars.auth.application.port.in.LoginUseCase;
import com.example.cars.auth.application.port.in.RegisterCommand;
import com.example.cars.auth.application.port.in.RegisterUserUseCase;
import com.example.cars.auth.infrastructure.adapter.in.web.dto.AuthResponse;
import com.example.cars.auth.infrastructure.adapter.in.web.dto.LoginRequest;
import com.example.cars.auth.infrastructure.adapter.in.web.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUseCase loginUseCase;

    public AuthController(RegisterUserUseCase registerUserUseCase, LoginUseCase loginUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUseCase = loginUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        var result = registerUserUseCase.register(
                new RegisterCommand(request.name(), request.email(), request.password()));
        return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.from(result));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return AuthResponse.from(loginUseCase.login(
                new LoginCommand(request.email(), request.password())));
    }
}
