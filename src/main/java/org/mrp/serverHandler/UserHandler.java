package org.mrp.serverHandler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.mrp.exceptions.DuplicateResourceException;
import org.mrp.exceptions.ForbiddenException;
import org.mrp.model.Rating;
import org.mrp.model.User;
import org.mrp.service.UserService;
import org.mrp.utils.JsonHelper;
import org.mrp.utils.PathParameterExtraction;
import org.mrp.utils.TokenValidation;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class UserHandler implements HttpHandler {
    UserService userService = new UserService();
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

            String username = pathParameterExtraction.extractUsername(path);

            //Profile
            if (path.endsWith("/profile") && HttpMethod.GET.name().equals(usedMethod)) {
                handleGetProfile(exchange, username);
            } else if (path.endsWith("/profile") && HttpMethod.PATCH.name().equals(usedMethod)) {
                handleUpdateProfile(exchange, username, userId);
            }

            //Favorites
            else if (path.endsWith("/favorites") && HttpMethod.GET.name().equals(usedMethod)) {
                Map<String, Object> response = userService.getFavorites(username);
                JsonHelper.sendResponse(exchange, 200, response);
            }

            //Ratings
            else if (path.endsWith("/ratings") && HttpMethod.GET.name().equals(usedMethod)) {
                Map<String, Object> response = userService.getUserRatings(userId);
                JsonHelper.sendResponse(exchange, 200, response);
            }

            //Leaderboard
            else if (path.endsWith("/leaderboard") && HttpMethod.GET.name().equals(usedMethod)) {
                List<Map<String, Object>> leaderboard = userService.getLeaderboard();
                JsonHelper.sendResponse(exchange, 200, leaderboard);
            }

            //Recommendations
            else if (path.endsWith("/recommendations") && HttpMethod.GET.name().equals(usedMethod)) {
                String message = userService.getRecommendations(userId);
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
        } catch (DuplicateResourceException e) {
            JsonHelper.sendError(exchange, 409, e.getMessage());
        } catch (Exception e) {
            JsonHelper.sendError(exchange, 500, "Internal server error");
        }
    }

    private void handleGetProfile(HttpExchange exchange, String username) throws IOException, SQLException {
        if (Objects.equals(username, "")) return;

        try {
            Map<String, Object> response = userService.getProfile(username);
            JsonHelper.sendResponse(exchange, 200, response);
        } catch (Exception e) {
            throw e; //throw to "main"-handle methode
        }
    }

    private void handleUpdateProfile(HttpExchange exchange, String username, UUID userId) throws IOException, SQLException {
        if (Objects.equals(username, "")) return;

        try {
            //@SuppressWarnings("unchecked") //Java-Annotation -> compiler ignores/suppresses specific warning (about unchecked casts)
            Map<String, Object> fieldsToUpdate = JsonHelper.parseRequest(exchange, HashMap.class); //PATCH - request-body (contains fields to be updated)
            Map<String, Object> response = userService.updateProfile(username, userId, fieldsToUpdate);
            JsonHelper.sendResponse(exchange, 200, response);
        } catch (Exception e) {
            throw e; //throw to "main"-handle methode
        }
    }
}
