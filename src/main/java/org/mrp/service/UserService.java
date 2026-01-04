package org.mrp.service;

import org.mrp.exceptions.DuplicateResourceException;
import org.mrp.exceptions.ForbiddenException;
import org.mrp.model.*;
import org.mrp.repository.RatingRepository;
import org.mrp.repository.UserRepository;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

public class UserService {
    private UserRepository userRepository;
    private RatingRepository ratingRepository;

    public UserService() {
        this.userRepository = new UserRepository();
        this.ratingRepository = new RatingRepository();
    }

    //Unit Tests: Constructor for testing with mocked repositories
    UserService(UserRepository userRepository, RatingRepository ratingRepository) {
        this.userRepository = userRepository;
        this.ratingRepository = ratingRepository;
    }

    private User getUserFromUsername(String username) throws IOException, SQLException {
        //Find user
        ResultSet rs = userRepository.findByUsername(username);

        if (!rs.next()) {
            throw new NoSuchElementException("User not found");
        }

        return new User(
                rs.getObject("user_id", UUID.class),
                rs.getString("username"),
                rs.getString("password_hashed")
        );
    }

    public Map<String, Object> getProfile(String username) throws IOException, SQLException {
        //Create user
        User user = getUserFromUsername(username);

        //Load Statistics
        UUID userId = user.getUserId();
        user.setRatings_total(userRepository.getRatings_total(userId));
        user.setFavorites_total(userRepository.getFavorites_total(userId));
        user.setMediaEntriesCreated_total(userRepository.getMediaEntriesCreated_total(userId));
        user.setAvgScore(userRepository.getAvgScore(userId));

        //Response
        Map<String, Object> response = new HashMap<>();
        response.put("user", user);
        response.put("message", "User's profile with statistics read successfully");

        return response;
    }

    //Edit user profile
    public Map<String, Object> updateProfile(String username, UUID requesterId, Map<String, Object> fieldsToUpdate) throws IOException, SQLException {
        //Create user
        User user = getUserFromUsername(username);

        //Check if requester (user) = own profile user
        if (!user.getUserId().equals(requesterId)) {
            throw new ForbiddenException("Forbidden: profile ownership mismatch");
        }

        //Update request-body fields (username)
        if (fieldsToUpdate.containsKey("username")) {
            String newUsername = fieldsToUpdate.get("username").toString();

            if (newUsername == null || newUsername.trim().isEmpty()) {
                throw new IllegalArgumentException("Username cannot be empty");
            }
            if (newUsername.length() < 3 || newUsername.length() > 50) {
                throw new IllegalArgumentException("Username must be between 3 and 50 characters");
            }
            //newUsername already taken? (by another user)
            if ((!newUsername.equals(username)) && userRepository.isExistingUsername(newUsername)) {
                throw new DuplicateResourceException("Username is already in use");
            }

            userRepository.updateUsername(user.getUserId(), newUsername);
            user.setUsername(newUsername);
        }

        //Response
        Map<String, Object> response = new HashMap<>();
        response.put("profile", user);
        response.put("message", "User's profile with statistics updated successfully");

        return response;
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
        if (favoritesRS == null) {
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
        boolean isCommentVisible = resultSet.getBoolean("is_comment_visible");
        String comment = isCommentVisible ? resultSet.getString("comment") : "";
        int starsCt = resultSet.getInt("stars_ct");

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


    //Recommendations

    public Map<String, Object> getRecommendations(UUID userId) throws IOException, SQLException {

        // Get user's preferences from highly rated media (4+ stars)
        ResultSet preferences = userRepository.getUserTopRatedMediaEntries(userId);

        EnumMap<Genre, Integer> genre_ct = new EnumMap<>(Genre.class);
        EnumMap<MediaEntryType, Integer> mediaType_ct = new EnumMap<>(MediaEntryType.class);
        Map<Integer, Integer> ageRestriction_ct = new HashMap<>();

        //Check if at least 1 top-rated media entry is found - return empty recommendations if none
        if (!preferences.isBeforeFirst()) {
            Map<String, Object> response = new HashMap<>();
            response.put("recommendations", new ArrayList<MediaEntry>());
            response.put("criteriaGenres", new ArrayList<Genre>());
            response.put("criteriaMediaTypes", new ArrayList<MediaEntryType>());
            response.put("criteriaAgeRestrictions", new ArrayList<Integer>());
            response.put("message", "No top-rated media entries found to base recommendations on");
            return response;
        }

        while (preferences.next()) {
            //Genres
            String genresStr = preferences.getString("genres");
            if (genresStr != null && !genresStr.isEmpty()) {
                Arrays.stream(genresStr.split(","))      // split by comma
                        .map(String::trim)                 // remove extra whitespace
                        .map(g -> toEnum(g, Genre.class))  // convert to enum
                        .forEach(g -> incrementCount(genre_ct, g));
            }

            //Media entry type
            incrementCount(mediaType_ct, toEnum(preferences.getString("type"), MediaEntryType.class));

            //Age restriction
            int ageRestriction = preferences.getInt("age_restriction");
            if (!preferences.wasNull()) {
                incrementCount(ageRestriction_ct, ageRestriction);
            }
        }

        List<Genre> favoriteGenres = getTopKeys(genre_ct, 5); //Get top 5 favorite genres
        List<MediaEntryType> favoriteMediaTypes = getTopKeys(mediaType_ct, 2); //Get preferred media types (top 2)
        List<Integer> preferredAgeRestrictions = getTopKeys(ageRestriction_ct, 2); //Get preferred age restrictions (top 2)

        List<MediaEntry> recommendations = userRepository.findRecommendations(userId, favoriteGenres, favoriteMediaTypes, preferredAgeRestrictions);

        //Response
        Map<String, Object> response = new HashMap<>();
        response.put("recommendations", recommendations);
        response.put("criteriaGenres", favoriteGenres);
        response.put("criteriaMediaTypes", favoriteMediaTypes);
        response.put("criteriaAgeRestrictions", preferredAgeRestrictions);

        return response;
    }

    //Helper method
    private static <K> List<K> getTopKeys(Map<K, Integer> map, int topN) {
        return map.entrySet().stream()
                .sorted(Map.Entry.<K, Integer>comparingByValue().reversed())
                .limit(topN)
                .map(Map.Entry::getKey)
                .toList();
    }

    //Helper methode
    private <K> void incrementCount(Map<K, Integer> map, K key) {
        if (key != null) {
            map.put(key, map.getOrDefault(key, 0) + 1);
        }
    }

    //Helper methode: convert String to Enum
    private <E extends Enum<E>> E toEnum(String value, Class<E> enumClass) { //E extends Enum<E> -> Type only can be Enum
        if (value == null) return null;
        return Enum.valueOf(enumClass, value);
    }




}
