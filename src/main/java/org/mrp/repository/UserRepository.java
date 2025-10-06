package org.mrp.repository;

import org.mrp.database.Database;
import org.mrp.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

public class UserRepository {
    private static final Database db = Database.getInstance();

    //for registration (AuthService)
    public static UUID save(String username, String passwordHash) throws SQLException {
        return db.insert(
                "INSERT INTO users (username, password_hased) VALUES (?, ?)",
                username,
                passwordHash
        );
    }

    //for login (AuthService)
    public static void update(String token, UUID userId) throws SQLException {
        db.update(
                "INSERT INTO tokens (token, user_id, created_at) VALUES (?, ?, ?)",
                token,
                userId,
                new Timestamp(System.currentTimeMillis())
        );
    }

    public ResultSet findById(UUID id) {
        return null;
    }

    public static ResultSet findByUsername(String username) throws SQLException {
        return db.query(
                "SELECT user_id, username, password_hashed FROM users WHERE username = ?",
                username
        );
    }

    public ResultSet findAll() {
        return null;
    }

    public void delete(UUID id) {

    }

    public static boolean userAlreadyExists(String username) throws SQLException {
        return db.exists("SELECT * FROM users WHERE username = ?", username);
    }
}
