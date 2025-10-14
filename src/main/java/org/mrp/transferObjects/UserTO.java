package org.mrp.transferObjects;

import java.util.UUID;

public class UserTO {
    private String username;
    private String password_hashed;

    public UserTO(String username, String password_hashed) {
        this.username = username;
        this.password_hashed = password_hashed;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword_hashed() {
        return password_hashed;
    }
}
