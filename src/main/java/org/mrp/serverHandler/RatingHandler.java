package org.mrp.serverHandler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.mrp.service.RatingService;
import org.mrp.utils.JsonHelper;
import org.mrp.utils.TokenValidation;

import java.io.IOException;
import java.util.UUID;

public class RatingHandler implements HttpHandler {
    RatingService ratingService = new RatingService();
    TokenValidation tokenValidation = new TokenValidation();

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

            String ratingId = extractRatingId(path);

            if (path.endsWith("/confirm") && HttpMethod.PUT.name().equals(usedMethod)) {
                String message = ratingService.confirmComment(userId, ratingId);
                JsonHelper.sendSuccess(exchange, message);
            }
            else if(path.endsWith("/like") && HttpMethod.POST.name().equals(usedMethod)) {
                String message = ratingService.likeRating(userId, ratingId);
                JsonHelper.sendSuccess(exchange, message);
            }
            else if(path.endsWith("/unlike") && HttpMethod.DELETE.name().equals(usedMethod)) {
                String message = ratingService.unlikeRating(userId, ratingId);
                JsonHelper.sendSuccess(exchange, message);
            }

            //Create
            else if (HttpMethod.POST.name().equals(usedMethod)) {
                String message = ratingService.createRating(userId);
                JsonHelper.sendSuccess(exchange, message);
            }
            //Update
            else if (HttpMethod.PUT.name().equals(usedMethod)) {
                String message = ratingService.updateRating(userId, ratingId);
                JsonHelper.sendSuccess(exchange, message);
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
        } catch (IllegalArgumentException e) {
            JsonHelper.sendError(exchange, 400, e.getMessage());
        } catch (SecurityException e) {
            JsonHelper.sendError(exchange, 403, e.getMessage());
        } catch (Exception e) {
            JsonHelper.sendError(exchange, 500, "Internal server error");
        }
    }

    //Helperfunction to get the mediaEntry id from the path
    private String extractRatingId(String path) {
        //Extract id from paths like /rating/{id}
        String[] parts = path.split("/");
        //parts: ["", "rating", "{id}", ...]
        return parts.length > 2 ? parts[2] : "";
    }
}
