package org.mrp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mrp.model.User;
import org.mrp.repository.UserRepository;

import java.sql.ResultSet;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) //Mockito used
class AuthServiceTest {

    @Mock
    private UserRepository userRepository; //don't use real database

    @Mock
    private ResultSet mockResultSet; //"fake" resultSet without real database

    private AuthService authService; //tested class -> no mock

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository);
    } //called before every test (tests don't influence each other)

    //1.) Register - Exception should be thrown if username too short (< 3)
    @Test
    void register_ShouldThrowException_WhenUsernameTooShort() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register("ab", "password123")
        );
        assertEquals("Username must be between 3 and 50 characters", exception.getMessage());
    }

    //2.) Register - Exception should be thrown if password too short (< 6)
    @Test
    void register_ShouldThrowException_WhenPasswordTooShort() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register("validUser", "12345")
        );
        assertEquals("Password must be at least 6 characters", exception.getMessage());
    }

    //3.) Register - Exception should be thrown if username already taken
    @Test
    void register_ShouldThrowException_WhenUsernameAlreadyExists() throws Exception {
        when(userRepository.userAlreadyExists("existingUser")).thenReturn(true); //mocked to return true -> pretends username exists

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register("existingUser", "password123")
        );
        assertEquals("Username already exists", exception.getMessage());
    }

    //4.) Register - Success-Case: username not taken, user saved, return contains userid, username, success message, save() got called
    @Test
    void register_ShouldReturnUserId_WhenValidCredentials() throws Exception {
        UUID expectedUserId = UUID.randomUUID();
        when(userRepository.userAlreadyExists("validUser")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(expectedUserId); //Regardless of which user saved, always expectedUserId returned

        Map<String, Object> result = authService.register("validUser", "password123");

        assertEquals(expectedUserId, result.get("userId"));
        assertEquals("validUser", result.get("username"));
        assertEquals("User registered successfully", result.get("message"));
        verify(userRepository).save(any(User.class)); //save() got called
    }

    //5.) Login - Exception should be thrown if username not found or wrong password
    @Test
    void login_ShouldThrowException_WhenInvalidCredentials() throws Exception {
        when(userRepository.findByUsername("testUser")).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false); //username or password not found in database

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login("testUser", "wrongPassword")
        );
        assertEquals("Invalid username or password", exception.getMessage());
    }
}
