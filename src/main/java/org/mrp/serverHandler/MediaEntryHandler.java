package org.mrp.serverHandler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.mrp.service.AuthService;
import org.mrp.service.MediaEntryService;
import org.mrp.utils.JsonHelper;

import java.io.IOException;
import java.util.UUID;

public class MediaEntryHandler implements HttpHandler {
    MediaEntryService mediaEntryService = new MediaEntryService();
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

            String mediaEntryId = extractMediaEntryId(path);

            if (path.endsWith("/create") && HttpMethod.POST.name().equals(usedMethod)) {
                mediaEntryService.createMediaEntry(exchange, userId);
            } else if (path.endsWith("/update") && HttpMethod.PUT.name().equals(usedMethod)) {
                mediaEntryService.updateMediaEntry(exchange, userId, mediaEntryId);
            } else if (path.endsWith("/read") && HttpMethod.GET.name().equals(usedMethod)) {
                mediaEntryService.getMediaEntries(exchange);
            } else if (path.endsWith("/delete") && HttpMethod.DELETE.name().equals(usedMethod)) {
                mediaEntryService.deleteMediaEntry(exchange, userId, mediaEntryId);
            } else {
                JsonHelper.sendError(exchange, 404, "Endpoint not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonHelper.sendError(exchange, 500, "Internal server error");
        }
    }

    //Helperfunction to get the mediaEntry id from the path
    private String extractMediaEntryId(String path) {
        //Extract id from paths like /mediaEntry/{id}/update
        String[] parts = path.split("/");
        //parts: ["", "mediaEntry", "{id}", ...]
        return parts.length > 2 ? parts[2] : "";
    }
}