package org.mrp.serverHandler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.mrp.service.AuthService;
import org.mrp.service.RatingService;
import org.mrp.utils.JsonHelper;

import java.io.IOException;
import java.util.UUID;

public class RatingHandler implements HttpHandler {
    RatingService ratingService = new RatingService();
    AuthService authService = new AuthService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String usedMethod = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {

            //Check authentication
            UUID userId = authService.validateToken(exchange);
            if (userId == null) {
                JsonHelper.sendError(exchange, 401, "Authentication required");
                return;
            }

            String ratingId = extractRatingId(path);

            if (path.endsWith("/confirm") && HttpMethod.PUT.name().equals(usedMethod)) {
                ratingService.confirmComment(exchange, userId, ratingId);
            }
            else if(path.endsWith("/like") && HttpMethod.POST.name().equals(usedMethod)) {
                ratingService.likeRating(exchange, userId, ratingId);
            }
            else if(path.endsWith("/unlike") && HttpMethod.DELETE.name().equals(usedMethod)) {
                ratingService.unlikeRating(exchange, userId, ratingId);
            }

            //Create
            else if (HttpMethod.POST.name().equals(usedMethod)) {
                ratingService.createRating(exchange, userId);
            }
            //Update
            else if (HttpMethod.PUT.name().equals(usedMethod)) {
                ratingService.updateRating(exchange, userId, ratingId);
            }
            //Delete
            else if (HttpMethod.DELETE.name().equals(usedMethod)) {
                ratingService.deleteRating(exchange, userId, ratingId);
            }

            //Send Error - Not found
            else {
                JsonHelper.sendError(exchange, 404, "Endpoint not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
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
