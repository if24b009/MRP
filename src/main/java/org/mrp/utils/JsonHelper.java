package org.mrp.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // Import für Java Time Module
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class JsonHelper {
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        //Configure ObjectMapper for Jackson Java Time Support (Timestamp)
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        //Register JavaTimeModul for LocalDateTime and other Java 8 Time-Types
        mapper.registerModule(new JavaTimeModule());
        //Prevent write Date-Objects as Timestamps
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // Parse JSON from request body
    public static <T> T parseRequest(HttpExchange exchange, Class<T> clazz) throws IOException {
        byte[] body = exchange.getRequestBody().readAllBytes();

        if (body.length == 0) {
            throw new IOException("Empty request body");
        }

        return mapper.readValue(body, clazz);
    }

    // Convert object to JSON string
    public static String toJson(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (Exception e) {
            return "{}";
        }
    }

    // Send JSON response
    public static void sendResponse(HttpExchange exchange, int statusCode, Object response) throws IOException {
        String jsonResponse = toJson(response);
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    // Send error response
    public static void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        sendResponse(exchange, statusCode, error);
    }

    // Send success response with message
    public static void sendSuccess(HttpExchange exchange, String message) throws IOException {
        Map<String, String> success = new HashMap<>();
        success.put("message", message);
        sendResponse(exchange, 200, success);
    }

    // Parse query parameters from URL
    public static Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query != null && !query.isEmpty()) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                } else if (keyValue.length == 1) {
                    params.put(keyValue[0], "");
                }
            }
        }
        return params;
    }
}
