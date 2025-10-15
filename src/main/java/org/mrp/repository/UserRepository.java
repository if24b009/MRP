package org.mrp.repository;

import org.mrp.database.Database;
import org.mrp.model.User;
import org.mrp.dto.UserTO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

public class UserRepository implements Repository<User, UserTO> {
    private Database db = new Database();

    public UserRepository() {
    }

    //for registration (AuthService)
    @Override
    public UUID save(UserTO user) throws SQLException {
        return db.insert(
                "INSERT INTO app_user (user_id, username, password_hashed) VALUES (?, ?, ?)",
                user.getUsername(),
                user.getPassword_hashed()
        );
    }

    //for login (AuthService) -> Token
    public void update(String token, UUID userId) throws SQLException {
        db.update(
                "INSERT INTO token (token, user_id, created_at) VALUES (?, ?, ?) ON CONFLICT (user_id) DO UPDATE SET token = (?), created_at = (?)",
                token,
                userId,
                new Timestamp(System.currentTimeMillis()),
                token,
                new Timestamp(System.currentTimeMillis())
        );
    }

    public UUID getUUID(ResultSet rs, String columnName) throws SQLException {
        return db.getUUID(rs, columnName);
    }

    @Override
    public ResultSet findById(UUID id) {
        return null;
    }

    public ResultSet findByUsername(String username) throws SQLException {
        return db.query(
                "SELECT user_id, username, password_hashed FROM app_user WHERE username = ?",
                username
        );
    }

    @Override
    public ResultSet findAll() {
        return null;
    }

    @Override
    public void delete(UUID id) {

    }

    public boolean userAlreadyExists(String username) throws SQLException {
        return db.exists("SELECT * FROM app_user WHERE username = ?", username);
    }
}
