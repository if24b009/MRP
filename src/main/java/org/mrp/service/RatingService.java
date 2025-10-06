package org.mrp.service;

import com.sun.net.httpserver.HttpExchange;
import org.mrp.model.Rating;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class RatingService {
    public static void createRating(HttpExchange exchange, UUID userId, UUID mediaEntryId) throws IOException, SQLException {

    }

    public static void updateRating(HttpExchange exchange, UUID userId, UUID ratingId) throws IOException, SQLException {

    }

    public static void deleteRating(HttpExchange exchange, UUID ratingId) throws IOException, SQLException {

    }

    public static List<Rating> getMediaEntryRatings(HttpExchange exchange, UUID mediaEntryId) throws IOException, SQLException {
        return null;
    }

    public static List<Rating> getUserRatings(HttpExchange exchange, UUID userId) throws IOException, SQLException {
        return null;
    }

    public static boolean canBeEditedBy(HttpExchange exchange, UUID userId, UUID ratingId) throws IOException, SQLException {
        return false;
    }

    public static boolean canBeDeletedBy(HttpExchange exchange, UUID userId, UUID ratingId) throws IOException, SQLException {
        return false;
    }
}
