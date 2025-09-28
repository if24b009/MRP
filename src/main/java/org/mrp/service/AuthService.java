package org.mrp.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.sun.net.httpserver.HttpExchange;
import org.mrp.model.Token;
import org.mrp.model.User;
import org.mrp.utils.JsonHelper;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthService {
    public static void register(HttpExchange exchange) throws IOException, SQLException {
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

        //Password hashing
        String passwordHash = BCrypt.withDefaults().hashToString(12, password.toCharArray());

        String userId = "0189e8c6-6b1b-7def-b95b-6f2b8cdffd5a"; //HARDCODED

        //Response
        Map<String, Object> response = new HashMap<>();
        response.put("id", userId);
        response.put("username", username);
        response.put("message", "User registered successfully");

        JsonHelper.sendResponse(exchange, 201, response);
    }

    public static void login(HttpExchange exchange) throws IOException, SQLException {
        Map<String, String> request = JsonHelper.parseRequest(exchange, HashMap.class);
        String username = request.get("username");
        String password = request.get("password");

        //HARDCODED -> DB !!!!
        String userId = "0189e8c6-6b1b-7def-b95b-6f2b8cdffd5a";
        String testUsername = "TestUser";
        String testPassword = "TestPassword";


        //Validate username
        //CHECK WITH DB !!!!
        if (username == null || username.trim().isEmpty()) {
            JsonHelper.sendError(exchange, 400, "Username needs to be entered");
            return;
        }
        if (!username.equals(testUsername)) {
            JsonHelper.sendError(exchange, 400, "Invalid username");
            return;
        }

        //Verify password
        //CHECK WITH DB !!!!
        if (password == null || password.trim().isEmpty()) {
            JsonHelper.sendError(exchange, 400, "Password needs to be entered");
            return;
        }
        if (!password.equals(testPassword)) {
            JsonHelper.sendError(exchange, 400, "Invalid password");
            return;
        }

        //Response - Token
        String token = username + "-" + UUID.randomUUID().toString(); //generate token
        //INSERT TOKEN IN DB !!!!
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("message", "User logged in successfully");

        JsonHelper.sendResponse(exchange, 201, response);
    }
}
