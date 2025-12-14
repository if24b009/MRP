package org.mrp.service;

import org.mrp.model.MediaEntry;
import org.mrp.model.Rating;
import org.mrp.repository.RatingRepository;
import org.mrp.repository.UserRepository;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

public class UserService {
    private UserRepository userRepository = new UserRepository();
    private RatingRepository ratingRepository = new RatingRepository();

    public String getProfile(String username) throws IOException, SQLException {
        return "Will be implemented soon";
    }

    //public XXX updateProfile()

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

    public Map<String, Object> getUserRatings(UUID userId) throws IOException, SQLException {
        //Query all ratings for the given user
        ResultSet resultSet = ratingRepository.findByUserId(userId);
        List<Rating> ratings = new ArrayList<>();

        while (resultSet.next()) {
            Rating rating = mapResultSetToRating(resultSet);
            ratings.add(rating);
        }

        //Response
        Map<String, Object> response = new HashMap<>();
        response.put("ratings", ratings);
        response.put("message", "Ratings for user read successfully");

        return response;
    }


    public Rating mapResultSetToRating(ResultSet resultSet) throws SQLException {
        //Extract columns from ResultSet
        UUID id = resultSet.getObject("id", UUID.class);
        UUID userId = resultSet.getObject("user_id", UUID.class);
        UUID mediaEntryId = resultSet.getObject("media_entry_id", UUID.class);
        String comment = resultSet.getString("comment");
        int starsCt = resultSet.getInt("stars_ct");
        boolean isCommentVisible = resultSet.getBoolean("is_comment_visible");

        LocalDateTime timestamp = null;
        Timestamp ts = resultSet.getTimestamp("timestamp");
        if (ts != null) {
            timestamp = ts.toLocalDateTime();
        }

        //Create Rating object using your constructor
        Rating rating = new Rating(userId, mediaEntryId, starsCt, comment, timestamp);
        rating.setId(id);
        rating.setStars_ct(starsCt);

        if (isCommentVisible) {
            rating.setCommentVisible();
        }

        //Initialize likedBy as empty list using the setter
        rating.setLikedBy(new ArrayList<>());

        return rating;
    }



    public List<Map<String, Object>> getLeaderboard() throws IOException, SQLException {
        ResultSet rs = userRepository.getLeaderboard();
        List<Map<String, Object>> leaderboard = new ArrayList<>();

        int pos = 1; //ranking/position (in list) of users based on media entries likes

        while (rs.next()) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("rank", pos++);
            entry.put("username", rs.getString("username"));
            entry.put("ratingCount", rs.getInt("rating_count"));
            entry.put("mediaCreated", rs.getInt("media_created"));
            entry.put("likesGiven", rs.getInt("likes_given"));
            entry.put("totalActivity", rs.getInt("rating_count") + rs.getInt("media_created") + rs.getInt("likes_given"));
            leaderboard.add(entry);
        }

        return leaderboard;
    }

    public String getRecommendations(UUID userId) throws IOException, SQLException {
        return "Will be implemented soon";
    }
}
