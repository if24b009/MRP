package org.mrp.repository;

import org.mrp.database.Database;
import org.mrp.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

public class UserRepository {
    private Database db = new Database();

    public UserRepository() {
    }

    //for registration (AuthService)
    public UUID save(String username, String passwordHash) throws SQLException {
        return db.insert(
                "INSERT INTO app_user (user_id, username, password_hashed) VALUES (?, ?, ?)",
                username,
                passwordHash
        );
    }

    //for login (AuthService)
    public void update(String token, UUID userId) throws SQLException {
        db.update(
                "INSERT INTO token (token, user_id, created_at) VALUES (?, ?, ?)",
                token,
                userId,
                new Timestamp(System.currentTimeMillis())
        );
    }

    public UUID getUUID(ResultSet rs, String columnName) throws SQLException {
        return db.getUUID(rs, columnName);
    }

    public ResultSet findById(UUID id) {
        return null;
    }

    public ResultSet findByUsername(String username) throws SQLException {
        return db.query(
                "SELECT user_id, username, password_hashed FROM app_user WHERE username = ?",
                username
        );
    }

    public ResultSet findAll() {
        return null;
    }

    public void delete(UUID id) {

    }

    public boolean userAlreadyExists(String username) throws SQLException {
        return db.exists("SELECT * FROM app_user WHERE username = ?", username);
    }
}
