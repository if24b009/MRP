package org.mrp.model;

import java.util.UUID;

public class User {
    private UUID userId;
    private String username;
    private String password_hashed;

    public User(UUID userId, String username, String password_hashed) {
        this.userId = userId;
        this.username = username;
        this.password_hashed = password_hashed;
    }

    public String getUsername() {
        return username;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getPassword() {
        return password_hashed;
    }

}
