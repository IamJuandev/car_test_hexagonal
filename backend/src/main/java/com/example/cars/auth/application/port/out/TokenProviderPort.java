package com.example.cars.auth.application.port.out;

import com.example.cars.users.domain.UserId;

import java.util.Optional;

/** Outbound port for issuing and validating auth tokens. JWT implementation lives in infrastructure. */
public interface TokenProviderPort {

    String generateToken(UserId userId);

    /** Returns the user id encoded in a valid token, or empty if the token is invalid/expired. */
    Optional<UserId> validateAndExtractUserId(String token);
}
