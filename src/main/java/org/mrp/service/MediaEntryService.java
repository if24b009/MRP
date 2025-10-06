package org.mrp.service;

import com.sun.net.httpserver.HttpExchange;
import org.mrp.model.MediaEntry;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

public class MediaEntryService {
    public static void createMediaEntry(HttpExchange exchange, UUID userId) throws IOException, SQLException {

    }

    public static void updateMediaEntry(HttpExchange exchange, UUID userId) throws IOException, SQLException {

    }

    public static void deleteMediaEntry(HttpExchange exchange, UUID userId) throws IOException, SQLException {

    }

    public static MediaEntry getMediaEntry(HttpExchange exchange, UUID mediaEntryId) throws IOException, SQLException {
        return null;
    }

    public static boolean canBeEditedBy(HttpExchange exchange, UUID userId, UUID mediaEntryId) throws IOException, SQLException {
        return false;
    }

    public static boolean canBeDeletedBy(HttpExchange exchange, UUID userId, UUID mediaEntryId) throws IOException, SQLException {
        return false;
    }

    public static boolean isUserCreator(HttpExchange exchange, UUID userId, UUID mediaEntryId) throws IOException, SQLException {
        return false;
    }
}
