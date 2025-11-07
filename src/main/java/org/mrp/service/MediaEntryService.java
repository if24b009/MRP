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

    //Media Entry CRUD

    public Map<String, Object> createMediaEntry(MediaEntry mediaEntry, UUID userId) throws IOException, SQLException {
        //Input validation
        if (mediaEntry.getTitle() == null || mediaEntry.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (isInvalidType(mediaEntry)) {
            throw new IllegalArgumentException("Media entry type must be 'movie', 'series', or 'game'");
        }

        //Insert MediaEntry with UUID in DB
        UUID mediaEntryId = mediaEntryRepository.save(new MediaEntryTO(null, mediaEntry.getTitle(), mediaEntry.getDescription(), mediaEntry.getType(), mediaEntry.getReleaseYear(), mediaEntry.getAgeRestriction(), mediaEntry.getGenres(), userId));

        //Update MediaEntry with id and creator
        mediaEntry.setId(mediaEntryId);
        mediaEntry.setCreator(userId);

        //Response
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("mediaentry", mediaEntry);
        response.put("message", "MediaEntry created successfully");

        return response;
    }

    public Map<String, Object> updateMediaEntry(MediaEntry mediaEntry, UUID userId, String mediaEntryId) throws IOException, SQLException {
        //Check if user = creator
        Object creatorId_object = mediaEntryRepository.getCreatorObject(UUID.fromString(mediaEntryId));
        if (creatorId_object == null) {
            throw new NoSuchElementException("Media entry not found");
        }

        UUID creatorId = (UUID) creatorId_object;
        if (!creatorId.equals(userId)) {
            throw new IllegalArgumentException("Only the creator can edit this media");
        }

        //Update MediaEntry
        int updated = mediaEntryRepository.update(new MediaEntryTO(UUID.fromString(mediaEntryId), mediaEntry.getTitle(), mediaEntry.getDescription(), mediaEntry.getType(), mediaEntry.getReleaseYear(), mediaEntry.getAgeRestriction(), mediaEntry.getGenres(), userId));
        if (updated == 0) {
            throw new RuntimeException("Failed to update media entry");
        }
        mediaEntry.setId(UUID.fromString(mediaEntryId));

        //Response
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("mediaentry", mediaEntry);
        response.put("message", "Media entry updated successfully");

        return response;
    }

    public Map<String, Object> getMediaEntries() throws IOException, SQLException {
        ResultSet resultSet = mediaEntryRepository.findAll();
        List<MediaEntry> mediaEntries = new ArrayList<>();

        while (resultSet.next()) {
            MediaEntry mediaEntry = mapResultSetToMediaEntry(resultSet);
            mediaEntries.add(mediaEntry);
        }

        //Response
        Map<String, Object> response = new HashMap<>();
        response.put("mediaentries", mediaEntries);
        response.put("message", "Media entries read successfully");

        return response;
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
        if (createdAtTS != null) {
            mediaEntry.setCreatedAt(createdAtTS.toLocalDateTime());
        }

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


    public String deleteMediaEntry(UUID userId, String mediaEntryId) throws IOException, SQLException {
        //Check if user = creator
        Object creatorId_object = mediaEntryRepository.getCreatorObject(UUID.fromString(mediaEntryId));
        if (creatorId_object == null) {
            throw new NoSuchElementException("Media entry not found");
        }
        UUID creatorId = (UUID) creatorId_object;
        if (!creatorId.equals(userId)) {
            throw new IllegalArgumentException("Only the creator can delete this media entry");
        }

        //Delete mediaEntry (cascades to ratings, favorites, ...)
        int deleted = mediaEntryRepository.delete(UUID.fromString(mediaEntryId));

        if (deleted == 0) {
            throw new RuntimeException("Failed to delete media entry");
        }

        return "Media entry deleted successfully";
    }



    //Favorites

    public String addFavorite(UUID userId, String mediaEntryId) throws IOException, SQLException {
        UUID mediaUUID = UUID.fromString(mediaEntryId);

        // Check if media exists
        Object mediaExists = mediaEntryRepository.getCreatorObject(mediaUUID);
        if (mediaExists == null) {
            throw new NoSuchElementException("Media not found");
        }

        // Check if already favorited
        if (mediaEntryRepository.isFavorite(userId, mediaUUID)) {
            throw new IllegalArgumentException("Already in favorites");
        }

        // Add to favorites
        mediaEntryRepository.addFavorite(userId, mediaUUID);
        return "Favorite successfully added";
    }

    public String removeFavorite(UUID userId, String mediaEntryId) throws IOException, SQLException {
        UUID mediaUUID = UUID.fromString(mediaEntryId);

        int deleted = mediaEntryRepository.removeFavorite(userId, mediaUUID);
        if (deleted > 0) {
            return "Favorite successfully removed";
        } else {
            throw new NoSuchElementException("Not in favorites");
        }
    }
}
