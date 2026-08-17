package com.example.cars.users.domain;

import com.example.cars.shared.domain.InvalidInputException;

/**
 * A registered user. The domain stores only the password HASH; hashing itself
 * is an outbound port (infrastructure concern), never done here.
 */
public class User {

    private final UserId id; // null until persisted
    private final String name;
    private final Email email;
    private final String passwordHash;

    private User(UserId id, String name, Email email, String passwordHash) {
        if (name == null || name.isBlank()) {
            throw new InvalidInputException("Name is required");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new InvalidInputException("Password hash is required");
        }
        this.id = id;
        this.name = name.trim();
        this.email = email;
        this.passwordHash = passwordHash;
    }

    /** Create a brand-new user (not yet persisted, so no id). */
    public static User register(String name, Email email, String passwordHash) {
        return new User(null, name, email, passwordHash);
    }

    /** Rebuild a user loaded from persistence. */
    public static User reconstitute(UserId id, String name, Email email, String passwordHash) {
        if (id == null) {
            throw new InvalidInputException("Persisted user requires an id");
        }
        return new User(id, name, email, passwordHash);
    }

    public UserId id() {
        return id;
    }

    public String name() {
        return name;
    }

    public Email email() {
        return email;
    }

    public String passwordHash() {
        return passwordHash;
    }
}
