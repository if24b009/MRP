package org.mrp.utils;

import com.sun.net.httpserver.HttpExchange;
import org.mrp.exceptions.AuthenticationException;
import org.mrp.repository.UserRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class TokenValidation {
    UserRepository userRepository = new UserRepository();

    public UUID validateToken(HttpExchange exchange) throws SQLException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.substring(7); //Remove "Bearer " from header

        try {
            ResultSet resultSet = userRepository.findByToken(token);
            if (resultSet.next()) {
                return userRepository.getUUID(resultSet, "user_id");
            }
        } catch (SQLException e) {
            throw new AuthenticationException("Token validation failed", e);
        }

        return null;
    }
}
