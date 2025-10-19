package org.mrp.service;

import com.sun.net.httpserver.HttpExchange;
import org.mrp.repository.UserRepository;
import org.mrp.utils.JsonHelper;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class UserService {
    private UserRepository userRepository = new UserRepository();

    public void getProfile(HttpExchange exchange, String username) throws IOException, SQLException {
        JsonHelper.sendSuccess(exchange, "Will be implemented soon");
    }

    public void getFavorites(HttpExchange exchange, String username) throws IOException, SQLException {
        JsonHelper.sendSuccess(exchange, "Will be implemented soon");
    }

    public void getUserRatings(HttpExchange exchange, String username, UUID userId) throws IOException, SQLException {
        JsonHelper.sendSuccess(exchange, "Will be implemented soon");
    }

    public void getLeaderboard(HttpExchange exchange) throws IOException, SQLException {
        JsonHelper.sendSuccess(exchange, "Will be implemented soon");
    }

    public void getRecommendations(HttpExchange exchange, UUID userId) throws IOException, SQLException {
        JsonHelper.sendSuccess(exchange, "Will be implemented soon");
    }
}
