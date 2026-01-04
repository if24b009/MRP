package org.mrp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mrp.exceptions.ForbiddenException;
import org.mrp.model.Rating;
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

    private RatingService ratingService;

    @BeforeEach
    void setUp() {
        ratingService = new RatingService(ratingRepository);
    }

    @Test
    void createRating_ShouldThrowException_WhenStarsOutOfRange() {
        Rating rating = new Rating();
        rating.setStars_ct(0);
        UUID userId = UUID.randomUUID();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ratingService.createRating(rating, userId)
        );
        assertEquals("Stars can only be between 1 and 5", exception.getMessage());

        rating.setStars_ct(6);
        IllegalArgumentException exception2 = assertThrows(
                IllegalArgumentException.class,
                () -> ratingService.createRating(rating, userId)
        );
        assertEquals("Stars can only be between 1 and 5", exception2.getMessage());
    }

    @Test
    void createRating_ShouldSucceed_WhenValidRating() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID ratingId = UUID.randomUUID();
        UUID mediaEntryId = UUID.randomUUID();

        Rating rating = new Rating(null, mediaEntryId, 4, "Test comment", null);

        when(ratingRepository.save(any(Rating.class))).thenReturn(ratingId);

        Map<String, Object> result = ratingService.createRating(rating, userId);

        assertEquals(userId, result.get("userId"));
        assertEquals("Rating created successfully", result.get("message"));
        verify(ratingRepository).save(any(Rating.class));
    }

    @Test
    void updateRating_ShouldThrowForbiddenException_WhenNotCreator() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        UUID ratingId = UUID.randomUUID();

        Rating rating = new Rating();
        rating.setStars_ct(3);

        when(ratingRepository.getCreatorObject(ratingId)).thenReturn(creatorId);

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> ratingService.updateRating(rating, userId, ratingId)
        );
        assertEquals("Only the creator can edit this rating", exception.getMessage());
    }

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
        assertEquals("Only the creator can edit this rating", exception.getMessage());
    }

    @Test
    void likeRating_ShouldThrowException_WhenAlreadyLiked() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID ratingId = UUID.randomUUID();

        when(ratingRepository.getRatingObject(ratingId)).thenReturn(new Object());
        when(ratingRepository.isAlreadyLikedByUser(userId, ratingId)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ratingService.likeRating(userId, ratingId)
        );
        assertEquals("User already liked the rating", exception.getMessage());
    }

    @Test
    void unlikeRating_ShouldThrowException_WhenNotLiked() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID ratingId = UUID.randomUUID();

        when(ratingRepository.getRatingObject(ratingId)).thenReturn(new Object());
        when(ratingRepository.isAlreadyLikedByUser(userId, ratingId)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ratingService.unlikeRating(userId, ratingId)
        );
        assertEquals("User has not liked the rating yet", exception.getMessage());
    }
}
