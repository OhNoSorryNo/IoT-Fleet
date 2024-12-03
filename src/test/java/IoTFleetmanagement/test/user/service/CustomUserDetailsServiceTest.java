package IoTFleetmanagement.test.user.service;

import IoTFleetManagement.user.model.Role;
import IoTFleetManagement.user.model.User;
import IoTFleetManagement.user.repository.UserRepository;
import IoTFleetManagement.user.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link CustomUserDetailsService} class.
 */
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User mockUser;

    /**
     * Sets up test data and initializes mocks before each test.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create mock user and role
        Role role = new Role();
        role.setName("ROLE_USER");

        mockUser = new User();
        mockUser.setUsername("testUser");
        mockUser.setPassword("testPassword");
        mockUser.setRole(role);
    }

    /**
     * Tests the {@link CustomUserDetailsService#loadUserByUsername(String)} method for a valid username.
     */
    @Test
    void testLoadUserByUsername_Success() {
        // Arrange
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(mockUser));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testUser");

        // Assert
        assertNotNull(userDetails, "UserDetails should not be null");
        assertEquals("testUser", userDetails.getUsername(), "Username should match");
        assertEquals("testPassword", userDetails.getPassword(), "Password should match");
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")), "Authorities should include ROLE_USER");
    }

    /**
     * Tests the {@link CustomUserDetailsService#loadUserByUsername(String)} method for an invalid username.
     */
    @Test
    void testLoadUserByUsername_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername("invalidUser")).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("invalidUser"),
                "Should throw UsernameNotFoundException for an invalid username");

        assertEquals("User not found with username: invalidUser", exception.getMessage(),
                "Exception message should match");
    }

    /**
     * Tests that the repository is called with the correct username.
     */
    @Test
    void testRepositoryCall() {
        // Arrange
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(mockUser));

        // Act
        customUserDetailsService.loadUserByUsername("testUser");

        // Assert
        verify(userRepository, times(1)).findByUsername("testUser");
    }
}
