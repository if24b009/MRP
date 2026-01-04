package org.mrp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mrp.exceptions.ForbiddenException;
import org.mrp.exceptions.InvalidQueryParameterException;
import org.mrp.model.MediaEntry;
import org.mrp.model.MediaEntryType;
import org.mrp.repository.MediaEntryRepository;
import org.mrp.repository.RatingRepository;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaEntryServiceTest {

    @Mock
    private MediaEntryRepository mediaEntryRepository;

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private ResultSet mockResultSet;

    private MediaEntryService mediaEntryService;

    @BeforeEach
    void setUp() {
        mediaEntryService = new MediaEntryService(mediaEntryRepository, ratingRepository);
    }

    @Test
    void createMediaEntry_ShouldThrowException_WhenTitleIsEmpty() {
        MediaEntry mediaEntry = new MediaEntry();
        mediaEntry.setTitle("");
        mediaEntry.setType(MediaEntryType.MOVIE);
        UUID userId = UUID.randomUUID();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> mediaEntryService.createMediaEntry(mediaEntry, userId)
        );
        assertEquals("Title is required", exception.getMessage());
    }

    @Test
    void createMediaEntry_ShouldThrowException_WhenTypeIsInvalid() {
        MediaEntry mediaEntry = new MediaEntry();
        mediaEntry.setTitle("Valid Title");
        mediaEntry.setType(null);
        UUID userId = UUID.randomUUID();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> mediaEntryService.createMediaEntry(mediaEntry, userId)
        );
        assertEquals("Media entry type must be 'movie', 'series', or 'game'", exception.getMessage());
    }

    @Test
    void createMediaEntry_ShouldSucceed_WhenValidInput() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID mediaEntryId = UUID.randomUUID();

        MediaEntry mediaEntry = new MediaEntry();
        mediaEntry.setTitle("Test Movie");
        mediaEntry.setType(MediaEntryType.MOVIE);

        when(mediaEntryRepository.save(any(MediaEntry.class))).thenReturn(mediaEntryId);
        when(mediaEntryRepository.getCreated_at(mediaEntryId)).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getTimestamp("created_at")).thenReturn(new Timestamp(System.currentTimeMillis()));

        Map<String, Object> result = mediaEntryService.createMediaEntry(mediaEntry, userId);

        assertEquals(userId, result.get("userId"));
        assertEquals("MediaEntry created successfully", result.get("message"));
        verify(mediaEntryRepository).save(any(MediaEntry.class));
    }

    @Test
    void updateMediaEntry_ShouldThrowForbiddenException_WhenNotCreator() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        UUID mediaEntryId = UUID.randomUUID();

        MediaEntry mediaEntry = new MediaEntry();
        mediaEntry.setTitle("Updated Title");

        when(mediaEntryRepository.getCreatorObject(mediaEntryId)).thenReturn(creatorId);

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> mediaEntryService.updateMediaEntry(mediaEntry, userId, mediaEntryId)
        );
        assertEquals("Only the creator can edit this media", exception.getMessage());
    }

    @Test
    void addFavorite_ShouldThrowException_WhenAlreadyFavorited() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID mediaEntryId = UUID.randomUUID();

        when(mediaEntryRepository.getCreatorObject(mediaEntryId)).thenReturn(UUID.randomUUID());
        when(mediaEntryRepository.isFavorite(userId, mediaEntryId)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> mediaEntryService.addFavorite(userId, mediaEntryId)
        );
        assertEquals("Already in favorites", exception.getMessage());
    }

    @Test
    void getMediaEntries_ShouldThrowException_WhenInvalidFilterKey() {
        Map<String, String> filters = new HashMap<>();
        filters.put("invalidFilter", "someValue");

        InvalidQueryParameterException exception = assertThrows(
                InvalidQueryParameterException.class,
                () -> mediaEntryService.getMediaEntries(filters, "title")
        );
        assertEquals("Invalid filter: invalidFilter", exception.getMessage());
    }
}
