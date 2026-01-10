package org.mrp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mrp.exceptions.ForbiddenException;
import org.mrp.model.Rating;
import org.mrp.repository.MediaEntryRepository;
import org.mrp.repository.RatingRepository;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private MediaEntryRepository mediaEntryRepository;

    private RatingService ratingService;

    @BeforeEach
    void setUp() {
        ratingService = new RatingService(ratingRepository, mediaEntryRepository);
    }

    //1.) Create - Stars only 1-5 -> should throw exception if out of range
    @Test
    void createRating_ShouldThrowException_WhenStarsOutOfRange() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID mediaEntryId = UUID.randomUUID();

        //mock stars 0 (< 1)
        Rating rating = new Rating(null, mediaEntryId, 0, null, null);

        //Mock media entry exists
        when(mediaEntryRepository.getCreatorObject(mediaEntryId)).thenReturn(UUID.randomUUID());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ratingService.createRating(rating, userId)
        );
        assertEquals("Stars can only be between 1 and 5", exception.getMessage());

        //mock stars 6 (> 5)
        Rating rating2 = new Rating(null, mediaEntryId, 6, null, null);
        IllegalArgumentException exception2 = assertThrows(
                IllegalArgumentException.class,
                () -> ratingService.createRating(rating2, userId)
        );
        assertEquals("Stars can only be between 1 and 5", exception2.getMessage());
    }

    //2.) Create - Success-Case
    @Test
    void createRating_ShouldSucceed_WhenValidRating() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID ratingId = UUID.randomUUID();
        UUID mediaEntryId = UUID.randomUUID();

        //Create rating to pass to createRating()
        Rating rating = new Rating(null, mediaEntryId, 4, "Test comment", null);

        //Mock media entry exists
        when(mediaEntryRepository.getCreatorObject(mediaEntryId)).thenReturn(UUID.randomUUID());

        when(ratingRepository.save(any(Rating.class))).thenReturn(ratingId);

        Map<String, Object> result = ratingService.createRating(rating, userId);

        assertEquals(userId, result.get("userId"));
        assertEquals("Rating created successfully", result.get("message"));
        verify(ratingRepository).save(any(Rating.class)); //save() got called
    }

    //3.) Update - Only creator can edit -> should throw exception
    @Test
    void updateRating_ShouldThrowForbiddenException_WhenNotCreator() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        UUID ratingId = UUID.randomUUID();

        Rating rating = new Rating();
        rating.setStars_ct(3);

        when(ratingRepository.getCreatorObject(ratingId)).thenReturn(creatorId); //mock creatorId to be owner (not userId)

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> ratingService.updateRating(rating, userId, ratingId)
        ); //.updateRating() -> userId should be creatorId to be successful
        assertEquals("Only the creator can edit this rating", exception.getMessage());
    }

    //4.) Delete - Only creator can delete -> should throw exception
    @Test
    void deleteRating_ShouldThrowForbiddenException_WhenNotCreator() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        UUID ratingId = UUID.randomUUID();

        when(ratingRepository.getCreatorObject(ratingId)).thenReturn(creatorId);

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> ratingService.deleteRating(userId, ratingId)
        );
        assertEquals("Only the creator can delete this rating", exception.getMessage());
    }

    //5.) Like - Should throw exception if user likes rating twice
    @Test
    void likeRating_ShouldThrowException_WhenAlreadyLiked() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID ratingId = UUID.randomUUID();

        when(ratingRepository.getRatingObject(ratingId)).thenReturn(new Object()); //mock rating exists
        when(ratingRepository.isAlreadyLikedByUser(userId, ratingId)).thenReturn(true); //mock already liked by user

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ratingService.likeRating(userId, ratingId)
        );
        assertEquals("User already liked the rating", exception.getMessage());
    }

    //6.) Unlike - Only unlike when already liked -> should throw exception
    @Test
    void unlikeRating_ShouldThrowException_WhenNotLiked() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID ratingId = UUID.randomUUID();

        when(ratingRepository.getRatingObject(ratingId)).thenReturn(new Object()); //mock rating exists
        when(ratingRepository.isAlreadyLikedByUser(userId, ratingId)).thenReturn(false); //mock not liked by user

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ratingService.unlikeRating(userId, ratingId)
        );
        assertEquals("User has not liked the rating yet", exception.getMessage());
    }
}
