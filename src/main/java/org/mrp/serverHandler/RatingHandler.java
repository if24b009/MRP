package org.mrp.serverHandler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.mrp.exceptions.ForbiddenException;
import org.mrp.model.Rating;
import org.mrp.service.RatingService;
import org.mrp.utils.JsonHelper;
import org.mrp.utils.PathParameterExtraction;
import org.mrp.utils.TokenValidation;
import org.postgresql.util.PSQLException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

public class RatingHandler implements HttpHandler {
    RatingService ratingService = new RatingService();
    TokenValidation tokenValidation = new TokenValidation();
    PathParameterExtraction pathParameterExtraction = new PathParameterExtraction();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String usedMethod = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {

            //Check authentication
            UUID userId = tokenValidation.validateToken(exchange);
            if (userId == null) {
                JsonHelper.sendError(exchange, 401, "Authentication required");
                return;
            }

            UUID ratingId = pathParameterExtraction.extractId(exchange, path);

            if (path.endsWith("/confirm") && HttpMethod.PUT.name().equals(usedMethod)) {
                String message = ratingService.confirmComment(userId, ratingId);
                JsonHelper.sendSuccess(exchange, message);
            }
            else if(path.endsWith("/like") && HttpMethod.POST.name().equals(usedMethod)) {
                Map<String, Object> response = ratingService.likeRating(userId, ratingId);
                JsonHelper.sendResponse(exchange, 200, response);
            }
            else if(path.endsWith("/unlike") && HttpMethod.DELETE.name().equals(usedMethod)) {
                Map<String, Object> response = ratingService.unlikeRating(userId, ratingId);
                JsonHelper.sendResponse(exchange, 200, response);
            }

            //Create
            else if (HttpMethod.POST.name().equals(usedMethod)) {
                handleCreateRating(exchange, userId);
            }
            //Update
            else if (HttpMethod.PUT.name().equals(usedMethod)) {
                handleUpdateRating(exchange, userId, ratingId);
            }
            //Delete
            else if (HttpMethod.DELETE.name().equals(usedMethod)) {
                String message = ratingService.deleteRating(userId, ratingId);
                JsonHelper.sendSuccess(exchange, message);
            }

            //Send Error - Not found
            else {
                JsonHelper.sendError(exchange, 404, "Endpoint not found");
            }
        } catch (NoSuchElementException e) {
            JsonHelper.sendError(exchange, 404, e.getMessage());
        } catch (IllegalArgumentException e) {
            JsonHelper.sendError(exchange, 400, e.getMessage());
        } catch (ForbiddenException | SecurityException e) {
            JsonHelper.sendError(exchange, 403, e.getMessage());
        } catch (SQLException e) {
            JsonHelper.sendError(exchange, 409, e.getMessage());
        } catch (Exception e) {
            JsonHelper.sendError(exchange, 500, "Internal server error");
        }
    }

    private void handleCreateRating(HttpExchange exchange, UUID userId) throws IOException, SQLException {
        Rating rating = pathParameterExtraction.parseRequestOrSendError(exchange, Rating.class);
        if (rating == null) return;

        try {
            Map<String, Object> response = ratingService.createRating(rating, userId);
            JsonHelper.sendResponse(exchange, 200, response);
        } catch (Exception e) {
            throw e; //throw to "main"-handle methode
        }
    }

    private void handleUpdateRating(HttpExchange exchange, UUID userId, UUID ratingId) throws IOException, SQLException {
        Rating rating = pathParameterExtraction.parseRequestOrSendError(exchange, Rating.class);
        if (rating == null) return;

        try {
            Map<String, Object> response = ratingService.updateRating(rating, userId, ratingId);
            JsonHelper.sendResponse(exchange, 200, response);
        } catch (Exception e) {
            throw e; //throw to "main"-handle methode
        }
    }
}
