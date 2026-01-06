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

    //1.) GetProfile - Should throw exception if user doesn't exist
    @Test
    void getProfile_ShouldThrowException_WhenUserNotFound() throws Exception {
        when(userRepository.findByUsername("unknownUser")).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false); //mock user not found

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> userService.getProfile("unknownUser")
        );
        assertEquals("User not found", exception.getMessage());
    }

    //2.) UpdateProfile - Only profile owner can edit profile -> should throw exception
    @Test
    void updateProfile_ShouldThrowForbiddenException_WhenRequesterNotOwner() throws Exception {
        UUID profileUserId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        when(userRepository.findByUsername("testUser")).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true); //mock user exists
        when(mockResultSet.getObject("user_id", UUID.class)).thenReturn(profileUserId); //profile belongs to profileUserId
        when(mockResultSet.getString("username")).thenReturn("testUser"); //mock username = "testUser"
        when(mockResultSet.getString("password_hashed")).thenReturn("hashedPwd"); //mock password = "hashedPwd"

        Map<String, Object> updates = new HashMap<>();
        updates.put("username", "newName"); //mock update username

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> userService.updateProfile("testUser", requesterId, updates)
        ); //.updateProfile() -> requesterId should be profileUserId to be successful
        assertEquals("Forbidden: profile ownership mismatch", exception.getMessage());
    }

    //3.) UpdateProfile - Should throw exception if username too short (< 3)
    @Test
    void updateProfile_ShouldThrowException_WhenNewUsernameTooShort() throws Exception {
        UUID userId = UUID.randomUUID();

        when(userRepository.findByUsername("testUser")).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getObject("user_id", UUID.class)).thenReturn(userId); //mock user_id = userId
        when(mockResultSet.getString("username")).thenReturn("testUser");
        when(mockResultSet.getString("password_hashed")).thenReturn("hashedPwd");

        Map<String, Object> updates = new HashMap<>();
        updates.put("username", "ab"); //mock update username = "ab"

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateProfile("testUser", userId, updates)
        );
        assertEquals("Username must be between 3 and 50 characters", exception.getMessage());
    }

    //4.) UpdateProfile - Should throw exception if new username already taken
    @Test
    void updateProfile_ShouldThrowDuplicateException_WhenUsernameAlreadyTaken() throws Exception {
        UUID userId = UUID.randomUUID();

        when(userRepository.findByUsername("testUser")).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getObject("user_id", UUID.class)).thenReturn(userId);
        when(mockResultSet.getString("username")).thenReturn("testUser");
        when(mockResultSet.getString("password_hashed")).thenReturn("hashedPwd");
        when(userRepository.isExistingUsername("takenUsername")).thenReturn(true); //mock new username exists

        Map<String, Object> updates = new HashMap<>();
        updates.put("username", "takenUsername"); //mock update username to existing "takenUsername"

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> userService.updateProfile("testUser", userId, updates)
        );
        assertEquals("Username is already in use", exception.getMessage());
    }

    //5.) GetRecommendations - Should throw exception if user doesn't have top-rated media entries
    @Test
    void getRecommendations_ShouldThrowException_WhenNoTopRatedMedia() throws Exception {
        UUID userId = UUID.randomUUID();

        when(userRepository.getUserTopRatedMediaEntries(userId)).thenReturn(mockResultSet);
        when(mockResultSet.isBeforeFirst()).thenReturn(false); //mock no top-rated media entries

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> userService.getRecommendations(userId)
        );
        assertEquals("No top-rated media entries from the user", exception.getMessage());
    }
}