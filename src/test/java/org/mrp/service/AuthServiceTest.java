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

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ResultSet mockResultSet;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository);
    }

    @Test
    void register_ShouldThrowException_WhenUsernameTooShort() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register("ab", "password123")
        );
        assertEquals("Username must be between 3 and 50 characters", exception.getMessage());
    }

    @Test
    void register_ShouldThrowException_WhenPasswordTooShort() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register("validUser", "12345")
        );
        assertEquals("Password must be at least 6 characters", exception.getMessage());
    }

    @Test
    void register_ShouldThrowException_WhenUsernameAlreadyExists() throws Exception {
        when(userRepository.userAlreadyExists("existingUser")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register("existingUser", "password123")
        );
        assertEquals("Username already exists", exception.getMessage());
    }

    @Test
    void register_ShouldReturnUserId_WhenValidCredentials() throws Exception {
        UUID expectedUserId = UUID.randomUUID();
        when(userRepository.userAlreadyExists("validUser")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(expectedUserId);

        Map<String, Object> result = authService.register("validUser", "password123");

        assertEquals(expectedUserId, result.get("userId"));
        assertEquals("validUser", result.get("username"));
        assertEquals("User registered successfully", result.get("message"));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void login_ShouldThrowException_WhenInvalidCredentials() throws Exception {
        when(userRepository.findByUsername("testUser")).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login("testUser", "wrongPassword")
        );
        assertEquals("Invalid username or password", exception.getMessage());
    }
}
