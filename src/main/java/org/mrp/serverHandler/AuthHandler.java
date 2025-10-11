package org.mrp.serverHandler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.mrp.service.AuthService;
import org.mrp.utils.JsonHelper;

import java.io.IOException;


//for Root/Authentication "/" path
public class AuthHandler implements HttpHandler {
    AuthService authService = new AuthService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String usedMethod = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if (path.endsWith("/register") && HttpMethod.POST.name().equals(usedMethod)) {
                authService.register(exchange);
            } else if (path.endsWith("/login") && HttpMethod.POST.name().equals(usedMethod)) {
                authService.login(exchange);
            } else if (path.equals("/") || path.equals("/api") || path.equals("/api/")) { //Check: Correct endpoint?
                JsonHelper.sendResponse(exchange, 200,
                        java.util.Map.of(
                                "status", "ok",
                                "service", "Media Entries Ratings Platform",
                                "version", "1.0.0"
                        )
                );
            } else {
                JsonHelper.sendError(exchange, 404, "Endpoint not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonHelper.sendError(exchange, 500, "Internal server error");
        }
    }
}
