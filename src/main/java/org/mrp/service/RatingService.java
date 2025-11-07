package org.mrp.service;

import com.sun.net.httpserver.HttpExchange;
import org.mrp.repository.RatingRepository;
import org.mrp.utils.JsonHelper;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

public class RatingService {
    private RatingRepository ratingRepository = new RatingRepository();

    public String confirmComment(UUID userId, UUID ratingId) throws IOException, SQLException {
        return "Will be implemented soon";
    }

    public String likeRating(UUID userId, UUID ratingId) throws IOException, SQLException {
        return "Will be implemented soon";
    }

    public String unlikeRating(UUID userId, UUID ratingId) throws IOException, SQLException {
        return  "Will be implemented soon";
    }

    public String createRating(UUID userId) throws IOException, SQLException {
        return "Will be implemented soon";
    }

    public String updateRating(UUID userId, UUID ratingId) throws IOException, SQLException {
       return "Will be implemented soon";
    }

    public String deleteRating(UUID userId, UUID ratingId) throws IOException, SQLException {
        return "Will be implemented soon";
    }

}
