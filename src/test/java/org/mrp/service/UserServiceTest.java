package org.mrp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mrp.exceptions.DuplicateResourceException;
import org.mrp.exceptions.ForbiddenException;
import org.mrp.repository.RatingRepository;
import org.mrp.repository.UserRepository;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private ResultSet mockResultSet;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, ratingRepository);
    }

    @Test
    void getProfile_ShouldThrowException_WhenUserNotFound() throws Exception {
        when(userRepository.findByUsername("unknownUser")).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> userService.getProfile("unknownUser")
        );
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void updateProfile_ShouldThrowForbiddenException_WhenRequesterNotOwner() throws Exception {
        UUID profileUserId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        when(userRepository.findByUsername("testUser")).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getObject("user_id", UUID.class)).thenReturn(profileUserId);
        when(mockResultSet.getString("username")).thenReturn("testUser");
        when(mockResultSet.getString("password_hashed")).thenReturn("hashedPwd");

        Map<String, Object> updates = new HashMap<>();
        updates.put("username", "newName");

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> userService.updateProfile("testUser", requesterId, updates)
        );
        assertEquals("Forbidden: profile ownership mismatch", exception.getMessage());
    }

    @Test
    void updateProfile_ShouldThrowException_WhenNewUsernameTooShort() throws Exception {
        UUID userId = UUID.randomUUID();

        when(userRepository.findByUsername("testUser")).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getObject("user_id", UUID.class)).thenReturn(userId);
        when(mockResultSet.getString("username")).thenReturn("testUser");
        when(mockResultSet.getString("password_hashed")).thenReturn("hashedPwd");

        Map<String, Object> updates = new HashMap<>();
        updates.put("username", "ab");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateProfile("testUser", userId, updates)
        );
        assertEquals("Username must be between 3 and 50 characters", exception.getMessage());
    }

    @Test
    void updateProfile_ShouldThrowDuplicateException_WhenUsernameAlreadyTaken() throws Exception {
        UUID userId = UUID.randomUUID();

        when(userRepository.findByUsername("testUser")).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getObject("user_id", UUID.class)).thenReturn(userId);
        when(mockResultSet.getString("username")).thenReturn("testUser");
        when(mockResultSet.getString("password_hashed")).thenReturn("hashedPwd");
        when(userRepository.isExistingUsername("takenUsername")).thenReturn(true);

        Map<String, Object> updates = new HashMap<>();
        updates.put("username", "takenUsername");

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> userService.updateProfile("testUser", userId, updates)
        );
        assertEquals("Username is already in use", exception.getMessage());
    }

    @Test
    void getRecommendations_ShouldThrowException_WhenNoTopRatedMedia() throws Exception {
        UUID userId = UUID.randomUUID();

        when(userRepository.getUserTopRatedMediaEntries(userId)).thenReturn(mockResultSet);
        when(mockResultSet.isBeforeFirst()).thenReturn(false);

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> userService.getRecommendations(userId)
        );
        assertEquals("No top-rated media entries from the user", exception.getMessage());
    }
}