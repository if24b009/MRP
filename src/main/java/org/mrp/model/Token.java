package org.mrp.model;

import java.sql.Timestamp;

public class Token {
    private String token;
    private String userId;
    private Timestamp createdAt;

    public Token() {
    } //only used for JSON

    public Token(String token, String userId, Timestamp createdAt) {
        this.token = token;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    public String getToken() {
        return token;
    }

    public String getUserId() {
        return userId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }
}
