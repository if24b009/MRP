package org.mrp.serverHandler;

import com.fasterxml.jackson.core.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.mrp.service.AuthService;
import org.mrp.utils.JsonHelper;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


//for Root/Authentication "/" path
public class AuthHandler implements HttpHandler {
    AuthService authService = new AuthService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String usedMethod = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if (path.endsWith("/register") && HttpMethod.POST.name().equals(usedMethod)) {
                handleRegister(exchange);
            } else if (path.endsWith("/login") && HttpMethod.POST.name().equals(usedMethod)) {
                handleLogin(exchange);
            } else if (path.equals("/") || path.equals("/api") || path.equals("/api/")) { //Check: Correct endpoint?
                JsonHelper.sendResponse(exchange, 200,
                        Map.of(
                                "status", "ok",
                                "service", "Media Entries Ratings Platform",
                                "version", "1.0.0"
                        )
                );
            } else {
                JsonHelper.sendError(exchange, 404, "Endpoint not found");
            }
        } catch (IllegalArgumentException e) {
            JsonHelper.sendError(exchange, 400, e.getMessage());
        } catch (Exception e) {
            JsonHelper.sendError(exchange, 500, "Internal server error");
        }
    }

    private void handleRegister(HttpExchange exchange) throws IOException, SQLException {
        Map.Entry<String, String> credentials = extractCredentials(exchange);
        if (credentials == null) return;

        try {
            Map<String, Object> response = authService.register(credentials.getKey(), credentials.getValue());
            JsonHelper.sendResponse(exchange, 201, response);
        } catch (Exception e) {
            throw e; //throw to "main"-handle methode
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException, SQLException {
        Map.Entry<String, String> credentials = extractCredentials(exchange);
        if (credentials == null) return;

        try {
            Map<String, Object> response = authService.login(credentials.getKey(), credentials.getValue());
            JsonHelper.sendResponse(exchange, 201, response);
        } catch (Exception e) {
            throw e; //throw to "main"-handle methode
        }
    }

    //Helperfunction ot extract username and password
    private Map.Entry<String, String> extractCredentials(HttpExchange exchange) throws IOException {
        Map<String, String> request;

        try {
            request = JsonHelper.parseRequest(exchange, HashMap.class);
        } catch (JsonParseException e) {
            JsonHelper.sendError(exchange, 400, "Invalid JSON format");
            return null;
        }

        String username = request.get("username");
        String password = request.get("password");

        if (username == null || password == null) {
            JsonHelper.sendError(exchange, 400, "Missing username or password");
            return null;
        }

        return Map.entry(username, password); //Key = username | value = password
    }

}
