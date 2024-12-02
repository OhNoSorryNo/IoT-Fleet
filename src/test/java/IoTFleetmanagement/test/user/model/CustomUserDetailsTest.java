package IoTFleetmanagement.test.user.model;

import IoTFleetManagement.user.model.CustomUserDetails;
import IoTFleetManagement.user.model.Role;
import IoTFleetManagement.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link CustomUserDetails} class.
 */
public class CustomUserDetailsTest {

    private User user;
    private Role role;
    private CustomUserDetails customUserDetails;

    /**
     * Sets up the test data before each test.
     */
    @BeforeEach
    void setUp() {
        role = new Role();
        role.setName("ROLE_USER");

        user = new User();
        user.setUsername("testUser");
        user.setPassword("testPassword");
        user.setRole(role);

        customUserDetails = new CustomUserDetails(user);
    }

    /**
     * Tests the {@link CustomUserDetails#getAuthorities()} method to ensure it returns the correct authorities.
     */
    @Test
    void testGetAuthorities() {
        Collection<? extends GrantedAuthority> authorities = customUserDetails.getAuthorities();
        assertNotNull(authorities, "Authorities should not be null");
        assertEquals(1, authorities.size(), "There should be one authority");
        assertEquals("ROLE_USER", authorities.iterator().next().getAuthority(), "Authority should match the user's role");
    }

    /**
     * Tests the {@link CustomUserDetails#getPassword()} method to ensure it returns the user's password.
     */
    @Test
    void testGetPassword() {
        assertEquals("testPassword", customUserDetails.getPassword(), "Password should match the user's password");
    }

    /**
     * Tests the {@link CustomUserDetails#getUsername()} method to ensure it returns the user's username.
     */
    @Test
    void testGetUsername() {
        assertEquals("testUser", customUserDetails.getUsername(), "Username should match the user's username");
    }

    /**
     * Tests the {@link CustomUserDetails#isAccountNonExpired()} method to ensure it always returns true.
     */
    @Test
    void testIsAccountNonExpired() {
        assertTrue(customUserDetails.isAccountNonExpired(), "Account should be non-expired");
    }

    /**
     * Tests the {@link CustomUserDetails#isAccountNonLocked()} method to ensure it always returns true.
     */
    @Test
    void testIsAccountNonLocked() {
        assertTrue(customUserDetails.isAccountNonLocked(), "Account should be non-locked");
    }

    /**
     * Tests the {@link CustomUserDetails#isCredentialsNonExpired()} method to ensure it always returns true.
     */
    @Test
    void testIsCredentialsNonExpired() {
        assertTrue(customUserDetails.isCredentialsNonExpired(), "Credentials should be non-expired");
    }

    /**
     * Tests the {@link CustomUserDetails#isEnabled()} method to ensure it always returns true.
     */
    @Test
    void testIsEnabled() {
        assertTrue(customUserDetails.isEnabled(), "User should be enabled");
    }

    /**
     * Tests the {@link CustomUserDetails#getUser()} method to ensure it returns the original user entity.
     */
    @Test
    void testGetUser() {
        assertEquals(user, customUserDetails.getUser(), "The returned user should match the original user entity");
    }
}
