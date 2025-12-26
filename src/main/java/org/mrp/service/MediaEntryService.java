package org.mrp.service;

import org.mrp.exceptions.ForbiddenException;
import org.mrp.model.Genre;
import org.mrp.model.Rating;
import org.mrp.model.MediaEntry;
import org.mrp.model.MediaEntryType;
import org.mrp.repository.MediaEntryRepository;
import org.mrp.repository.RatingRepository;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

public class MediaEntryService {
    private MediaEntryRepository mediaEntryRepository = new MediaEntryRepository();
    private RatingRepository ratingRepository = new RatingRepository();

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
        UUID mediaEntryId = mediaEntryRepository.save(new MediaEntry(null, mediaEntry.getTitle(), mediaEntry.getDescription(), mediaEntry.getType(), mediaEntry.getReleaseYear(), mediaEntry.getAgeRestriction(), mediaEntry.getGenres(), userId));

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

    public Map<String, Object> updateMediaEntry(MediaEntry mediaEntry, UUID userId, UUID mediaEntryId) throws IOException, SQLException {
        //Check if user = creator
        Object creatorId_object = mediaEntryRepository.getCreatorObject(mediaEntryId);
        if (creatorId_object == null) {
            throw new NoSuchElementException("Media entry not found");
        }
        UUID creatorId = (UUID) creatorId_object;
        if (!creatorId.equals(userId)) {
            throw new ForbiddenException("Only the creator can edit this media");
        }

        //Update MediaEntry
        int updated = mediaEntryRepository.update(new MediaEntry(mediaEntryId, mediaEntry.getTitle(), mediaEntry.getDescription(), mediaEntry.getType(), mediaEntry.getReleaseYear(), mediaEntry.getAgeRestriction(), mediaEntry.getGenres(), userId));
        if (updated == 0) {
            throw new RuntimeException("Failed to update media entry");
        }
        mediaEntry.setId(mediaEntryId);

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

    //public -> because reused for user get its favorite media entries
    public MediaEntry mapResultSetToMediaEntry(ResultSet resultSet) throws SQLException {
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


    public String deleteMediaEntry(UUID userId, UUID mediaEntryId) throws IOException, SQLException {
        //Check if user = creator
        Object creatorId_object = mediaEntryRepository.getCreatorObject(mediaEntryId);
        if (creatorId_object == null) {
            throw new NoSuchElementException("Media entry not found");
        }
        UUID creatorId = (UUID) creatorId_object;
        if (!creatorId.equals(userId)) {
            throw new ForbiddenException("Only the creator can delete this media entry");
        }

        //Delete mediaEntry (cascades to ratings, favorites, ...)
        int deleted = mediaEntryRepository.delete(mediaEntryId);

        if (deleted == 0) {
            throw new RuntimeException("Failed to delete media entry");
        }

        return "Media entry deleted successfully";
    }



    //Favorites

    public String addFavorite(UUID userId, UUID mediaEntryId) throws IOException, SQLException {
        //Check if media exists
        Object mediaExists = mediaEntryRepository.getCreatorObject(mediaEntryId);
        if (mediaExists == null) {
            throw new NoSuchElementException("Media not found");
        }

        //Check if already favorited
        if (mediaEntryRepository.isFavorite(userId, mediaEntryId)) {
            throw new IllegalArgumentException("Already in favorites");
        }

        //Add to favorites
        mediaEntryRepository.addFavorite(userId, mediaEntryId);
        return "Favorite successfully added";
    }

    public String removeFavorite(UUID userId, UUID mediaEntryId) throws IOException, SQLException {
        int deleted = mediaEntryRepository.removeFavorite(userId, mediaEntryId);
        if (deleted > 0) {
            return "Favorite successfully removed";
        } else {
            throw new NoSuchElementException("Not in favorites");
        }
    }

    public Map<String, Object> getMediaEntryRatings(UUID mediaEntryId) throws IOException, SQLException {
        //Query all ratings for the given media entry
        ResultSet resultSet = ratingRepository.findByMediaEntryId(mediaEntryId);
        List<Rating> ratings = new ArrayList<>();

        while (resultSet.next()) {
            Rating rating = mapResultSetToRating(resultSet);
            ratings.add(rating);
        }

        //Response
        Map<String, Object> response = new HashMap<>();
        response.put("ratings", ratings);
        response.put("message", "Ratings for media entry read successfully");

        return response;
    }

    public Rating mapResultSetToRating(ResultSet resultSet) throws SQLException {
        //Extract columns from ResultSet
        UUID id = resultSet.getObject("id", UUID.class);
        UUID userId = resultSet.getObject("user_id", UUID.class);
        UUID mediaEntryId = resultSet.getObject("media_entry_id", UUID.class);
        boolean isCommentVisible = resultSet.getBoolean("is_comment_visible");
        String comment = isCommentVisible ? resultSet.getString("comment") : "";
        int starsCt = resultSet.getInt("stars_ct");

        LocalDateTime timestamp = null;
        Timestamp ts = resultSet.getTimestamp("timestamp");
        if (ts != null) {
            timestamp = ts.toLocalDateTime();
        }

        //Create Rating object using your constructor
        Rating rating = new Rating(userId, mediaEntryId, starsCt, comment, timestamp);
        rating.setId(id);
        rating.setStars_ct(starsCt);

        if (isCommentVisible) {
            rating.setCommentVisible();
        }

        //Initialize likedBy as empty list using the setter
        rating.setLikedBy(new ArrayList<>());

        return rating;
    }

}
