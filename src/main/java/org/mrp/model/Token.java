package org.mrp.model;

import java.sql.Timestamp;
import java.util.UUID;

public class Token {
    private String token;
    private UUID userId;
    private Timestamp createdAt;

    public Token() {
    } //only used for JSON

    public Token(String token, UUID userId, Timestamp createdAt) {
        this.token = token;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    public String getToken() {
        return token;
    }

    public UUID getUserId() {
        return userId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }
}
