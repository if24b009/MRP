package org.mrp.serverHandler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.mrp.service.AuthService;
import org.mrp.service.UserService;
import org.mrp.utils.JsonHelper;

import java.io.IOException;
import java.util.UUID;

public class UserHandler implements HttpHandler {
    UserService userService = new UserService();
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

            String username = extractUsername(path);

            if (path.endsWith("/profile") && HttpMethod.GET.name().equals(usedMethod)) {
                userService.getProfile(exchange, username);
            } else if (path.endsWith("/favorites") && HttpMethod.GET.name().equals(usedMethod)) {
                userService.getFavorites(exchange, username);
            } else if (path.endsWith("/ratings") && HttpMethod.GET.name().equals(usedMethod)) {
                userService.getUserRatings(exchange, username, userId);
            } else if (path.endsWith("/leaderboard") && HttpMethod.GET.name().equals(usedMethod)) {
                userService.getLeaderboard(exchange);
            } else if (path.endsWith("/recommendations") && HttpMethod.GET.name().equals(usedMethod)) {
                userService.getRecommendations(exchange, userId);
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

    //Helperfunction to get the Username id from the path
    private String extractUsername(String path) {
        //Extract id from paths like /users/{username}/update
        String[] parts = path.split("/");
        //parts: ["", "users", "{username}", ...]
        return parts.length > 2 ? parts[2] : "";
    }
}
