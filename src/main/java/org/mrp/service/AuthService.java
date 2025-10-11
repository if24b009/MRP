package org.mrp.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.sun.net.httpserver.HttpExchange;
import org.mrp.repository.UserRepository;
import org.mrp.utils.JsonHelper;
import org.mrp.utils.UUIDGenerator;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthService {
    private UserRepository  userRepository = new UserRepository();

    public void register(HttpExchange exchange) throws IOException, SQLException {
        Map<String, String> request = JsonHelper.parseRequest(exchange, HashMap.class);
        String username = request.get("username");
        String password = request.get("password");

        //Input validation
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            JsonHelper.sendError(exchange, 400, "Username and password are required");
            return;
        }

        if (username.length() < 3 || username.length() > 50) {
            JsonHelper.sendError(exchange, 400, "Username must be between 3 and 50 characters");
            return;
        }

        if (password.length() < 6) {
            JsonHelper.sendError(exchange, 400, "Password must be at least 6 characters");
            return;
        }

        try {
            //Password hashing
            String passwordHash = BCrypt.withDefaults().hashToString(12, password.toCharArray());

            if(userRepository.userAlreadyExists(username)) {
                JsonHelper.sendError(exchange, 400, "Username already exists");
                return;
            }

            //userId gets generated while inserting user in database
            UUID userId = userRepository.save(username, passwordHash);

            //Response
            Map<String, Object> response = new HashMap<>();
            response.put("id", userId);
            response.put("username", username);
            response.put("message", "User registered successfully");

            JsonHelper.sendResponse(exchange, 201, response);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void login(HttpExchange exchange) throws IOException, SQLException {
        Map<String, String> request = JsonHelper.parseRequest(exchange, HashMap.class);
        String username = request.get("username");
        String password = request.get("password");

        //Validate username
        if (username == null || username.trim().isEmpty()) {
            JsonHelper.sendError(exchange, 400, "Username needs to be entered");
            return;
        }
        //Validate password
        if (password == null || password.trim().isEmpty()) {
            JsonHelper.sendError(exchange, 400, "Password needs to be entered");
            return;
        }

        try {
            //Find user
            ResultSet resultSet = userRepository.findByUsername(username);

            //Data exists?
            if (!resultSet.next()) JsonHelper.sendError(exchange, 400, "Invalid username or password");

            //Get userId and password
            UUID userId = userRepository.getUUID(resultSet, "user_id");
            String passwordHashed = resultSet.getString("password_hashed");

            //Verify password
            BCrypt.Result passwordIsVerified = BCrypt.verifyer().verify(password.toCharArray(), passwordHashed.toCharArray());
            if (!passwordIsVerified.verified) JsonHelper.sendError(exchange, 400, "Invalid password");


            //Response - Token
            String token = username + "-" + UUIDGenerator.generateUUIDv7(); //generate token

            //Insert Token in DB (Token-based login)
            userRepository.update(token, userId);

            //Response
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("message", "User logged in successfully");

            JsonHelper.sendResponse(exchange, 201, response);

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
