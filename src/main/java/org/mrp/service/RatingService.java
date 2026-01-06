package org.mrp.service;

import org.mrp.exceptions.ForbiddenException;
import org.mrp.model.Rating;
import org.mrp.repository.MediaEntryRepository;
import org.mrp.repository.RatingRepository;
import java.sql.SQLException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

public class RatingService {
    private final RatingRepository ratingRepository;
    private final MediaEntryRepository mediaEntryRepository;

    public RatingService() {
        this.ratingRepository = new RatingRepository();
        this.mediaEntryRepository = new MediaEntryRepository();
    }

    //Unit Tests: Constructor for testing with mocked repository
    RatingService(RatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
        this.mediaEntryRepository = new MediaEntryRepository();
    }
    public String confirmComment(UUID userId, UUID ratingId) throws IOException, SQLException {
        //Check if user = creator
        if (!isUserCreator(ratingId, userId)) {
            throw new ForbiddenException("Only the creator can edit this rating");
        }

        //Change visibility in DB
        int updated = ratingRepository.changeCommentVisibility(ratingId);
        if (updated == 0) {
            throw new NoSuchElementException("Rating not found");
        }

        return "Comment confirmed successfully for visibility";
    }

    public Map<String, Object> likeRating(UUID userId, UUID ratingId) throws IOException, SQLException {
        //Check if rating exists
        Object rating = ratingRepository.getRatingObject(ratingId);
        if (rating == null) {
            throw new NoSuchElementException("Rating not found");
        }

        //Check if already liked by user
        if (ratingRepository.isAlreadyLikedByUser(userId, ratingId)) {
            throw new IllegalArgumentException("User already liked the rating");
        }

        //Add to Like-User-List
        ratingRepository.addUserLike(userId, ratingId);

        //Response
        Map<String, Object> response = new HashMap<>();
        response.put("rating", rating);
        response.put("message", "Rating liked successfully");

        return response;
    }

    public Map<String, Object> unlikeRating(UUID userId, UUID ratingId) throws IOException, SQLException {
        //Check if rating exists
        Object rating = ratingRepository.getRatingObject(ratingId);
        if (rating == null) {
            throw new NoSuchElementException("Rating not found");
        }

        //Check if already liked by user
        if (!ratingRepository.isAlreadyLikedByUser(userId, ratingId)) {
            throw new IllegalArgumentException("User has not liked the rating yet");
        }

        //Remove from Like-User-List
        ratingRepository.removeUserLike(userId, ratingId);

        //Response
        Map<String, Object> response = new HashMap<>();
        response.put("rating", rating);
        response.put("message", "Rating unliked successfully");

        return response;
    }

    public Map<String, Object> createRating(Rating rating, UUID userId) throws IOException, SQLException {
        //Validate if mediaEntryId is provided
        if (rating.getMediaEntryId() == null) {
            throw new IllegalArgumentException("mediaEntryId is required");
        }

        //Check if media entry exists
        if (mediaEntryRepository.getCreatorObject(rating.getMediaEntryId()) == null) {
            throw new NoSuchElementException("Media entry not found");
        }

        if(rating.getStars_ct() > 5 || rating.getStars_ct() < 1) {
            throw new IllegalArgumentException("Stars can only be between 1 and 5");
        }

        try {
            //Insert Rating with UUID in DB
            UUID ratingId = ratingRepository.save(new Rating(userId, rating.getMediaEntryId(), rating.getStars_ct(), rating.getComment(), rating.getTimestamp()));

            //Update Rating with id
            rating.setId(ratingId);

            //Response
            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("rating", rating);
            response.put("message", "Rating created successfully");

            return response;

        } catch (SQLException e) {
            //Check if error is due to duplicate key violation
            if (e.getMessage().contains("duplicate key value violates unique constraint")) {
                throw new SQLException("Duplicate rating for this media entry and user.");
            }
            throw e; //Rethrow any other SQLExceptions
        }
    }

    public Map<String, Object> updateRating(Rating rating, UUID userId, UUID ratingId) throws IOException, SQLException {
        //Check if user = creator
        if (!isUserCreator(ratingId, userId)) {
            throw new ForbiddenException("Only the creator can edit this rating");
        }

        if(rating.getStars_ct() > 5 || rating.getStars_ct() < 1) {
            throw new IllegalArgumentException("Stars can only be between 1 and 5");
        }

        //Update Rating
        int updated = ratingRepository.update(new Rating(ratingId, userId, rating.getMediaEntryId(), rating.getStars_ct(), rating.getComment(), rating.getTimestamp()));
        if (updated == 0) {
            throw new RuntimeException("Failed to update rating");
        }
        rating.setId(ratingId);

        //Response
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("rating", rating);
        response.put("message", "Rating updated successfully");

        return response;
    }

    public String deleteRating(UUID userId, UUID ratingId) throws IOException, SQLException {
        //Check if user = creator
        if (!isUserCreator(ratingId, userId)) {
            throw new ForbiddenException("Only the creator can edit this rating");
        }

        //Delete rating
        int deleted = ratingRepository.delete(ratingId);
        if (deleted == 0) {
            throw new RuntimeException("Failed to delete rating");
        }

        return "Rating deleted successfully";
    }


    //Helper function: Check if user = creator
    private boolean isUserCreator(UUID id, UUID userId) throws SQLException {
        Object creatorId_object = ratingRepository.getCreatorObject(id);
        if (creatorId_object == null) {
            throw new NoSuchElementException("Rating not found");
        }
        UUID creatorId = (UUID) creatorId_object; //Parse to UUID
        if (!creatorId.equals(userId)) {
            return false;
        }
        return true;
    }
}
