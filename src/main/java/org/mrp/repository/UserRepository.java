package org.mrp.repository;

import org.mrp.model.User;
import org.mrp.dto.UserTO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

public class UserRepository implements Repository<User, UserTO> {
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

    public ResultSet findByToken(String token) throws SQLException {
        return db.query(
                "SELECT user_id FROM token WHERE token = ?",
                token
        );
    }

    @Override
    public ResultSet findAll() {
        return null;
    }

    @Override
    public int delete(UUID id) {
        return 0;
    }

    public boolean userAlreadyExists(String username) throws SQLException {
        return db.exists("SELECT * FROM app_user WHERE username = ?", username);
    }

    public ResultSet getFavoriteMediaEntries(UUID userId) throws SQLException {
        return db.query(
                """
                        SELECT
                            m.*,
                            STRING_AGG(meg.genre::TEXT, ',') AS genres,
                            u.username AS creator_username,
                            COALESCE(AVG(r.stars_ct), 0) AS avg_rating,
                            COUNT(DISTINCT r.id) AS total_ratings
                        FROM media_entry m
                        INNER JOIN favorite f
                            ON m.id = f.media_entry_id
                        INNER JOIN app_user u
                            ON m.creator_id = u.user_id
                        LEFT JOIN rating r
                            ON m.id = r.media_entry_id
                        LEFT JOIN media_entry_genre meg
                            ON m.id = meg.media_entry_id
                        WHERE f.user_id = ?
                        GROUP BY m.id, u.username
                        ORDER BY m.title ASC;
                        """,
                userId
        );
    }
}
