package org.mrp.serverHandler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.mrp.service.UserService;
import org.mrp.utils.JsonHelper;
import org.mrp.utils.PathParameterExtraction;
import org.mrp.utils.TokenValidation;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

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

            if (path.endsWith("/profile") && HttpMethod.GET.name().equals(usedMethod)) {
                String message = userService.getProfile(username);
                JsonHelper.sendSuccess(exchange, message);
            } else if (path.endsWith("/favorites") && HttpMethod.GET.name().equals(usedMethod)) {
                Map<String, Object> response = userService.getFavorites(username);
                JsonHelper.sendResponse(exchange, 200, response);
            } else if (path.endsWith("/ratings") && HttpMethod.GET.name().equals(usedMethod)) {
                Map<String, Object> response = userService.getUserRatings(userId);
                JsonHelper.sendResponse(exchange, 200, response);
            } else if (path.endsWith("/leaderboard") && HttpMethod.GET.name().equals(usedMethod)) {
                List<Map<String, Object>> leaderboard = userService.getLeaderboard();
                JsonHelper.sendResponse(exchange, 200, leaderboard);
            } else if (path.endsWith("/recommendations") && HttpMethod.GET.name().equals(usedMethod)) {
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
        } catch (SecurityException e) {
            JsonHelper.sendError(exchange, 403, e.getMessage());
        } catch (Exception e) {
            JsonHelper.sendError(exchange, 500, "Internal server error");
        }
    }
}
