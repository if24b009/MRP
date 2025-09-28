package org.mrp.service;

import com.sun.net.httpserver.HttpExchange;
import org.mrp.model.Rating;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class RatingService {
    public static void createRating(HttpExchange exchange, int userId, int mediaEntryId) throws IOException, SQLException {

    }

    public static void updateRating(HttpExchange exchange, int userId, int ratingId) throws IOException, SQLException {

    }

    public static void deleteRating(HttpExchange exchange, int ratingId) throws IOException, SQLException {

    }

    public static List<Rating> getMediaEntryRatings(HttpExchange exchange, int mediaEntryId) throws IOException, SQLException {
        return null;
    }

    public static List<Rating> getUserRatings(HttpExchange exchange, int userId) throws IOException, SQLException {
        return null;
    }

    public static boolean canBeEditedBy(HttpExchange exchange, int userId, int ratingId) throws IOException, SQLException {
        return false;
    }

    public static boolean canBeDeletedBy(HttpExchange exchange, int userId, int ratingId) throws IOException, SQLException {
        return false;
    }
}
