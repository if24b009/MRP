package org.mrp.utils;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.UUID;

public class PathParameterExtraction {
    //Helperfunction to get the id from the path and parse it safely to a UUID
    public UUID extractId(HttpExchange exchange, String path) throws IOException {
        //Extract id from paths like /rating/{id}
        String[] parts = path.split("/");
        //parts: ["", "rating", "{id}", ...]
        String uuid = parts.length > 2 ? parts[2] : "";
        UUID parsedUUID = null;
        try {
            if (uuid != null && !uuid.isEmpty()) {
                parsedUUID = UUID.fromString(uuid);
            }
        } catch (IllegalArgumentException e) {
            JsonHelper.sendError(exchange, 400, "Invalid UUID format");
            return null;
        }
        return parsedUUID;
    }

    //Helperfunction to get the Username id from the path
    public String extractUsername(String path) {
        //Extract id from paths like /users/{username}/update
        String[] parts = path.split("/");
        //parts: ["", "users", "{username}", ...]
        return parts.length > 2 ? parts[2] : "";
    }

    //Helperfunction to parse the request
    public <T> T parseRequestOrSendError(HttpExchange exchange, Class<T> c) throws IOException {
        try {
            return JsonHelper.parseRequest(exchange, c);
        } catch (IOException e) {
            JsonHelper.sendError(exchange, 400, "Invalid request");
            return null;
        }
    }
}
