package org.mrp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mrp.exceptions.DuplicateResourceException;
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

    //1.) Create - No empty Title allowed -> should throw exception
    @Test
    void createMediaEntry_ShouldThrowException_WhenTitleIsEmpty() {
        //Create Media Entry to pass to createMediaEntry()
        MediaEntry mediaEntry = new MediaEntry();
        mediaEntry.setTitle(""); //mock empty title
        mediaEntry.setType(MediaEntryType.MOVIE);
        UUID userId = UUID.randomUUID();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> mediaEntryService.createMediaEntry(mediaEntry, userId)
        );
        assertEquals("Title is required", exception.getMessage());
    }

    //2.) Create - Must have a valid type (not null) -> should throw exception
    @Test
    void createMediaEntry_ShouldThrowException_WhenTypeIsInvalid() {
        MediaEntry mediaEntry = new MediaEntry();
        mediaEntry.setTitle("Valid Title");
        mediaEntry.setType(null); //mock invalid type
        UUID userId = UUID.randomUUID();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> mediaEntryService.createMediaEntry(mediaEntry, userId)
        );
        assertEquals("Media entry type must be 'movie', 'series', or 'game'", exception.getMessage());
    }

    //3.) Create - Success-Case
    @Test
    void createMediaEntry_ShouldSucceed_WhenValidInput() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID mediaEntryId = UUID.randomUUID();

        MediaEntry mediaEntry = new MediaEntry();
        mediaEntry.setTitle("Test Movie");
        mediaEntry.setType(MediaEntryType.MOVIE);

        when(mediaEntryRepository.save(any(MediaEntry.class))).thenReturn(mediaEntryId); //"Fake-ID": Regardless of which media entry saved, always mediaEntryId returned

        //Simulate creation time
        when(mediaEntryRepository.getCreated_at(mediaEntryId)).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getTimestamp("created_at")).thenReturn(new Timestamp(System.currentTimeMillis()));

        Map<String, Object> result = mediaEntryService.createMediaEntry(mediaEntry, userId); //response

        assertEquals(userId, result.get("userId"));
        assertEquals("MediaEntry created successfully", result.get("message"));
        verify(mediaEntryRepository).save(any(MediaEntry.class)); //save() got called
    }

    //4.) Update - Only creator can edit -> should throw exception
    @Test
    void updateMediaEntry_ShouldThrowForbiddenException_WhenNotCreator() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        UUID mediaEntryId = UUID.randomUUID();

        MediaEntry mediaEntry = new MediaEntry();
        mediaEntry.setTitle("Updated Title");

        when(mediaEntryRepository.getCreatorObject(mediaEntryId)).thenReturn(creatorId); //mock creatorId to be owner (not userId)

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> mediaEntryService.updateMediaEntry(mediaEntry, userId, mediaEntryId)
        ); //.updateMediaEntry() -> userId should be creatorId to be successful
        assertEquals("Only the creator can edit this media", exception.getMessage());
    }

    //5.) AddFavorite - Should throw exception if media entry is marked as favorite twice
    @Test
    void addFavorite_ShouldThrowException_WhenAlreadyFavorited() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID mediaEntryId = UUID.randomUUID();

        when(mediaEntryRepository.getCreatorObject(mediaEntryId)).thenReturn(UUID.randomUUID()); //mock creatorId = randomUUID() -> don't test creator = user
        when(mediaEntryRepository.isFavorite(userId, mediaEntryId)).thenReturn(true); //mock already marked as favorite

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> mediaEntryService.addFavorite(userId, mediaEntryId)
        );
        assertEquals("Already in favorites", exception.getMessage());
    }

    //6.) getMediaEntries - Should throw exception if invalid filters set
    @Test
    void getMediaEntries_ShouldThrowException_WhenInvalidFilterKey() {
        Map<String, String> filters = new HashMap<>();
        filters.put("invalidFilter", "someValue"); //mock invalid filters

        InvalidQueryParameterException exception = assertThrows(
                InvalidQueryParameterException.class,
                () -> mediaEntryService.getMediaEntries(filters, "title")
        );
        assertEquals("Invalid filter: invalidFilter", exception.getMessage());
    }
}
