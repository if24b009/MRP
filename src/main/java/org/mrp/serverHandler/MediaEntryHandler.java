package org.mrp.serverHandler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.mrp.model.MediaEntry;
import org.mrp.service.MediaEntryService;
import org.mrp.utils.JsonHelper;
import org.mrp.utils.PathParameterExtraction;
import org.mrp.utils.TokenValidation;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

public class MediaEntryHandler implements HttpHandler {
    MediaEntryService mediaEntryService = new MediaEntryService();
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

            UUID mediaEntryId = pathParameterExtraction.extractId(exchange, path);

            //Favorites
            if (path.endsWith("/favorite")) {
                if (HttpMethod.POST.name().equals(usedMethod)) {
                    String message = mediaEntryService.addFavorite(userId, mediaEntryId);
                    JsonHelper.sendSuccess(exchange, message);
                } else if (HttpMethod.DELETE.name().equals(usedMethod)) {
                    String message = mediaEntryService.removeFavorite(userId, mediaEntryId);
                    JsonHelper.sendSuccess(exchange, message);
                }
            }


            //MediaEntry CRUD
            else if (HttpMethod.POST.name().equals(usedMethod)) {
                handleCreateMediaEntry(exchange, userId);
            } else if (HttpMethod.PUT.name().equals(usedMethod)) {
                handleUpdateMediaEntry(exchange, userId, mediaEntryId);
            } else if (HttpMethod.GET.name().equals(usedMethod)) {
                Map<String, Object> response = mediaEntryService.getMediaEntries();
                JsonHelper.sendResponse(exchange, 200, response);
            } else if (HttpMethod.DELETE.name().equals(usedMethod)) {
                String message = mediaEntryService.deleteMediaEntry(userId, mediaEntryId);
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

    private void handleCreateMediaEntry(HttpExchange exchange, UUID userId) throws IOException, SQLException {
        MediaEntry mediaEntry = parseRequestOrSendError(exchange, MediaEntry.class);
        if (mediaEntry == null) return;

        try {
            Map<String, Object> response = mediaEntryService.createMediaEntry(mediaEntry, userId);
            JsonHelper.sendResponse(exchange, 200, response);
        } catch (Exception e) {
            throw e; //throw to "main"-handle methode
        }
    }

    private void handleUpdateMediaEntry(HttpExchange exchange, UUID userId, UUID mediaEntryId) throws IOException, SQLException {
        MediaEntry mediaEntry = parseRequestOrSendError(exchange, MediaEntry.class);
        if (mediaEntry == null) return;

        try {
            Map<String, Object> response = mediaEntryService.updateMediaEntry(mediaEntry, userId, mediaEntryId);
            JsonHelper.sendResponse(exchange, 200, response);
        } catch (Exception e) {
            throw e; //throw to "main"-handle methode
        }
    }

    //Helperfunction to parse the request
    private <T> T parseRequestOrSendError(HttpExchange exchange, Class<T> c) throws IOException {
        try {
            return JsonHelper.parseRequest(exchange, c);
        } catch (IOException e) {
            JsonHelper.sendError(exchange, 400, "Invalid request");
            return null;
        }
    }
}