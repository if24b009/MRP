package org.mrp.service;

import org.mrp.model.MediaEntry;
import org.mrp.repository.UserRepository;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class UserService {
    private UserRepository userRepository = new UserRepository();

    public String getProfile(String username) throws IOException, SQLException {
        return "Will be implemented soon";
    }

    public Map<String, Object> getFavorites(String username) throws IOException, SQLException {
        //Find user
        ResultSet rs = userRepository.findByUsername(username);
        //User(-name) exists?
        if (!rs.next()) {
            throw new IllegalArgumentException("Invalid username");
        }

        UUID userId = userRepository.getUUID(rs, "user_id");

        ResultSet favoritesRS = userRepository.getFavoriteMediaEntries(userId);
        if(favoritesRS == null) {
            throw new NoSuchElementException("No favorites for this user found");
        }

        MediaEntryService mediaEntryService = new MediaEntryService(); //helper -> reuse maping function to get media entry
        List<MediaEntry> favorites = new ArrayList<>();
        while (favoritesRS.next()) {
            MediaEntry mediaEntry = mediaEntryService.mapResultSetToMediaEntry(favoritesRS);
            favorites.add(mediaEntry);
        }

        //Response
        Map<String, Object> response = new HashMap<>();
        response.put("favorites", favorites);
        response.put("message", "User's favorites read successfully");

        return response;
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
