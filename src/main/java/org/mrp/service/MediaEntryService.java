package org.mrp.service;

import com.sun.net.httpserver.HttpExchange;
import org.mrp.dto.MediaEntryTO;
import org.mrp.model.Genre;
import org.mrp.model.MediaEntry;
import org.mrp.model.MediaEntryType;
import org.mrp.repository.MediaEntryRepository;
import org.mrp.utils.JsonHelper;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

public class MediaEntryService {
    private MediaEntryRepository mediaEntryRepository = new MediaEntryRepository();

    private boolean isInvalidType(MediaEntry mediaEntry) {
        return mediaEntry.getType() == null ||
                (!mediaEntry.getType().equals(MediaEntryType.MOVIE)
                        && !mediaEntry.getType().equals(MediaEntryType.SERIES)
                        && !mediaEntry.getType().equals(MediaEntryType.GAME));
    }

    public void createMediaEntry(HttpExchange exchange, UUID userId) throws IOException, SQLException {
        MediaEntry mediaEntry = new MediaEntry();

        try {
            mediaEntry = JsonHelper.parseRequest(exchange, MediaEntry.class);
        }
        catch(IOException e){
            JsonHelper.sendError(exchange, 400, "Invalid request");
            return;
        }

        //Input validation
        if (mediaEntry.getTitle() == null || mediaEntry.getTitle().trim().isEmpty()) {
            JsonHelper.sendError(exchange, 400, "Title is required");
            return;
        }
        if (isInvalidType(mediaEntry)) {
            JsonHelper.sendError(exchange, 400, "MediaEntry type must be 'movie', 'series', or 'game'");
            return;
        }

        try {
            //Insert MediaEntry with UUID in DB
            UUID mediaEntryId = mediaEntryRepository.save(new MediaEntryTO(null, mediaEntry.getTitle(), mediaEntry.getDescription(), mediaEntry.getType(), mediaEntry.getReleaseYear(), mediaEntry.getAgeRestriction(), mediaEntry.getGenres(), userId));

            //Update MediaEntry with id and creator
            mediaEntry.setId(mediaEntryId);
            mediaEntry.setCreator(userId);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JsonHelper.sendResponse(exchange, 201, mediaEntry);
    }

    public void updateMediaEntry(HttpExchange exchange, UUID userId, String mediaEntryId) throws IOException, SQLException {
        try {
            //Check if user = creator
            Object creatorId_object = mediaEntryRepository.getCreatorObject(mediaEntryId);
            if (creatorId_object == null) {
                JsonHelper.sendError(exchange, 404, "MediaEntry not found");
                return;
            }

            UUID creatorId = UUID.fromString((String) creatorId_object);
            if (!creatorId.equals(userId)) {
                JsonHelper.sendError(exchange, 403, "Only the creator can edit this media");
                return;
            }

            MediaEntry mediaEntry = JsonHelper.parseRequest(exchange, MediaEntry.class);

            //Update MediaEntry
            int updated = mediaEntryRepository.update(new MediaEntryTO(UUID.fromString(mediaEntryId), mediaEntry.getTitle(), mediaEntry.getDescription(), mediaEntry.getType(), mediaEntry.getReleaseYear(), mediaEntry.getAgeRestriction(), mediaEntry.getGenres(), userId));
            if (updated > 0) {
                mediaEntry.setId(UUID.fromString(mediaEntryId));
                JsonHelper.sendResponse(exchange, 200, mediaEntry);
            } else {
                JsonHelper.sendError(exchange, 500, "Failed to update media");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void getMediaEntries(HttpExchange exchange) throws IOException, SQLException {
        ResultSet resultSet = mediaEntryRepository.findAll();
        List<MediaEntry> mediaEntries = new ArrayList<>();

        while (resultSet.next()) {
            MediaEntry mediaEntry = mapResultSetToMediaEntry(resultSet);
            mediaEntries.add(mediaEntry);
        }

        JsonHelper.sendResponse(exchange, 200, mediaEntries);
    }

    private MediaEntry mapResultSetToMediaEntry(ResultSet resultSet) throws SQLException {
        MediaEntry mediaEntry = new MediaEntry();

        mediaEntry.setId(mediaEntryRepository.getUUID(resultSet, "id"));
        mediaEntry.setTitle(resultSet.getString("title"));
        mediaEntry.setDescription(resultSet.getString("description"));

        String typeStr = resultSet.getString("type");
        if (typeStr != null) mediaEntry.setType(MediaEntryType.valueOf(typeStr.toUpperCase()));

        mediaEntry.setReleaseYear(resultSet.getInt("release_year"));
        mediaEntry.setAgeRestriction(resultSet.getInt("age_restriction"));
        mediaEntry.setCreator(mediaEntryRepository.getUUID(resultSet, "creator_id"));

        Timestamp createdAtTS = resultSet.getTimestamp("created_at");
        if (createdAtTS != null) mediaEntry.setCreatedAt(createdAtTS.toLocalDateTime());

        //Parse Genres from string -> Set Genres as List<Genre> (Enum)
        String genresString = resultSet.getString("genres");
        List<Genre> genreList = new ArrayList<>();
        if (genresString != null && !genresString.isEmpty()) {
            String[] genreNames = genresString.split(",");
            for (String name : genreNames) {
                try {
                    Genre genre = Genre.valueOf(name.trim().toUpperCase());
                    genreList.add(genre);
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid genre in db result: " + name);
                }
            }
        }
        mediaEntry.setGenres(genreList);

        return mediaEntry;
    }


    public void deleteMediaEntry(HttpExchange exchange, UUID userId, String mediaEntryId) throws IOException, SQLException {
        try {
            //Check if user = creator
            Object creatorId_object = mediaEntryRepository.getCreatorObject(mediaEntryId);
            if (creatorId_object == null) {
                JsonHelper.sendError(exchange, 404, "MediaEntry not found");
                return;
            }

            UUID creatorId = UUID.fromString((String) creatorId_object);
            if (!creatorId.equals(userId)) {
                JsonHelper.sendError(exchange, 403, "Only the creator can delete this media");
                return;
            }
            //Delete mediaEntry (cascades to ratings, favorites, ...)
            int deleted = mediaEntryRepository.delete(UUID.fromString(mediaEntryId));

            if (deleted > 0) {
                JsonHelper.sendSuccess(exchange, "MediaEntry deleted successfully");
            } else {
                JsonHelper.sendError(exchange, 500, "Failed to delete mediaEntry");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
