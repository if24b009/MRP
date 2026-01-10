package org.mrp.serverHandler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.mrp.exceptions.AuthenticationException;
import org.mrp.exceptions.DuplicateResourceException;
import org.mrp.exceptions.ForbiddenException;
import org.mrp.exceptions.InvalidQueryParameterException;
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

//Path "/mediaEntry"
public class MediaEntryHandler implements HttpHandler {
    MediaEntryService mediaEntryService = new MediaEntryService();
    TokenValidation tokenValidation = new TokenValidation();
    PathParameterExtraction pathParameterExtraction = new PathParameterExtraction();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String usedMethod = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();

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

            //Ratings to specific Media Entry
            else if (path.endsWith("/ratings") && HttpMethod.GET.name().equals(usedMethod)) {
                Map<String, Object> response = mediaEntryService.getMediaEntryRatings(mediaEntryId);
                JsonHelper.sendResponse(exchange, 200, response);
            }

            //MediaEntry CRUD
            else if (HttpMethod.POST.name().equals(usedMethod)) {
                handleCreateMediaEntry(exchange, userId);
            } else if (HttpMethod.PUT.name().equals(usedMethod)) {
                handleUpdateMediaEntry(exchange, userId, mediaEntryId);
            } else if (HttpMethod.GET.name().equals(usedMethod)) {
                handleGetMediaEntries(exchange, query);
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
        } catch (ForbiddenException | SecurityException e) {
            JsonHelper.sendError(exchange, 403, e.getMessage());
        } catch (DuplicateResourceException e) {
            JsonHelper.sendError(exchange, 409, e.getMessage());
        } catch (AuthenticationException e) {
            JsonHelper.sendError(exchange, 401, e.getMessage());
        } catch (InvalidQueryParameterException | IllegalArgumentException e) {
            JsonHelper.sendError(exchange, 400, e.getMessage());
        } catch (Exception e) {
            JsonHelper.sendError(exchange, 500, "Internal server error");
        }
    }

    private void handleCreateMediaEntry(HttpExchange exchange, UUID userId) throws IOException, SQLException {
        MediaEntry mediaEntry = pathParameterExtraction.parseRequestOrSendError(exchange, MediaEntry.class);
        if (mediaEntry == null) return;

        try {
            Map<String, Object> response = mediaEntryService.createMediaEntry(mediaEntry, userId);
            JsonHelper.sendResponse(exchange, 200, response);
        } catch (Exception e) {
            throw e; //throw to "main"-handle methode
        }
    }

    private void handleUpdateMediaEntry(HttpExchange exchange, UUID userId, UUID mediaEntryId) throws IOException, SQLException {
        MediaEntry mediaEntry = pathParameterExtraction.parseRequestOrSendError(exchange, MediaEntry.class);
        if (mediaEntry == null) return;

        try {
            Map<String, Object> response = mediaEntryService.updateMediaEntry(mediaEntry, userId, mediaEntryId);
            JsonHelper.sendResponse(exchange, 200, response);
        } catch (Exception e) {
            throw e; //throw to "main"-handle methode
        }
    }

    private void handleGetMediaEntries(HttpExchange exchange, String query) throws IOException, SQLException {
        Map<String, String> parameters = JsonHelper.parseQueryParams(query);

        try {
            String sortBy = parameters.getOrDefault("sortBy", "title"); //default sorted by title (sortBy != null)
            parameters.remove("sortBy"); //remove sortBy from filters -> avoid validation errors

            Map<String, Object> response = mediaEntryService.getMediaEntries(parameters, sortBy);
            JsonHelper.sendResponse(exchange, 200, response);
        } catch (Exception e) {
            throw e; //throw to "main"-handle methode
        }
    }
}