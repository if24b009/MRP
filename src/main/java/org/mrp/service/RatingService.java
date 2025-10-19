package org.mrp.service;

import com.sun.net.httpserver.HttpExchange;
import org.mrp.repository.RatingRepository;
import org.mrp.utils.JsonHelper;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

public class RatingService {
    private RatingRepository ratingRepository = new RatingRepository();

    public void confirmComment(HttpExchange exchange, UUID userId, String ratingId) throws IOException, SQLException {
        JsonHelper.sendSuccess(exchange, "Will be implemented soon");
    }

    public void likeRating(HttpExchange exchange, UUID userId, String ratingId) throws IOException, SQLException {
        JsonHelper.sendSuccess(exchange, "Will be implemented soon");
    }

    public void unlikeRating(HttpExchange exchange, UUID userId, String ratingId) throws IOException, SQLException {
        JsonHelper.sendSuccess(exchange, "Will be implemented soon");
    }

    public void createRating(HttpExchange exchange, UUID userId) throws IOException, SQLException {
        JsonHelper.sendSuccess(exchange, "Will be implemented soon");
    }

    public void updateRating(HttpExchange exchange, UUID userId, String ratingId) throws IOException, SQLException {
        JsonHelper.sendSuccess(exchange, "Will be implemented soon");
    }

    public void deleteRating(HttpExchange exchange, UUID userId, String ratingId) throws IOException, SQLException {
        JsonHelper.sendSuccess(exchange, "Will be implemented soon");
    }

}
