package org.mrp.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.mrp.repository.UserRepository;
import org.mrp.dto.UserTO;
import org.mrp.utils.UUIDGenerator;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthService {
    private UserRepository userRepository = new UserRepository();

    public Map<String, Object> register(String username, String password) throws IOException, SQLException {
        //Input validation
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Username and password are required");
        }

        if (username.length() < 3 || username.length() > 50) {
            throw new IllegalArgumentException("Username must be between 3 and 50 characters");
        }

        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        //Password hashing
        String passwordHash = BCrypt.withDefaults().hashToString(12, password.toCharArray());

        if (userRepository.userAlreadyExists(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        //userId gets generated while inserting user in database
        UUID userId = userRepository.save(new UserTO(username, passwordHash));

        //Response
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("username", username);
        response.put("message", "User registered successfully");

        return response;
    }

    public Map<String, Object> login(String username, String password) throws IOException, SQLException {
        //Validate username
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username needs to be entered");
        }
        //Validate password
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password needs to be entered");
        }

        //Find user
        ResultSet resultSet = userRepository.findByUsername(username);

        //Data exists?
        if (!resultSet.next()) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        //Get userId and password
        UUID userId = userRepository.getUUID(resultSet, "user_id");
        String passwordHashed = resultSet.getString("password_hashed");

        //Verify password
        BCrypt.Result passwordIsVerified = BCrypt.verifyer().verify(password.toCharArray(), passwordHashed.toCharArray());
        if (!passwordIsVerified.verified) {
            throw new IllegalArgumentException("Invalid password");
        }


        //Response - Token
        String token = username + "-" + UUIDGenerator.generateUUIDv7(); //generate token

        //Insert Token in DB (Token-based login)
        userRepository.update(token, userId);

        //Response
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("userId", userId);
        response.put("message", "User logged in successfully");

        return response;
    }
}
