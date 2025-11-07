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

    public String getProfile(String username) throws IOException, SQLException {
        return "Will be implemented soon";
    }

    public String getFavorites(String username) throws IOException, SQLException {
        return "Will be implemented soon";
    }

    public String getUserRatings(String username, UUID userId) throws IOException, SQLException {
        return "Will be implemented soon";
    }

    public String getLeaderboard() throws IOException, SQLException {
        return "Will be implemented soon";
    }

    public String getRecommendations(UUID userId) throws IOException, SQLException {
        return "Will be implemented soon";
    }
}
