package IoTFleetManagment.test.user.model;

import IoTFleetManagement.user.model.Role;
import IoTFleetManagement.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserTest {

    private User user;
    private Role role;

    @BeforeEach
    void setUp() {
        role = new Role("ROLE_USER");
        user = new User("test@example.com", "testUser", "testPassword", role);
    }

    @Test
    void testGetId() {
        // Arrange
        Long expectedId = 1L;
        user.setId(expectedId);

        // Act
        Long actualId = user.getId();

        // Assert
        assertEquals(expectedId, actualId);
    }

    @Test
    void testSetId() {
        // Arrange
        Long newId = 2L;

        // Act
        user.setId(newId);

        // Assert
        assertEquals(newId, user.getId());
    }

    @Test
    void testGetUsername() {
        // Act
        String actualUsername = user.getUsername();

        // Assert
        assertEquals("testUser", actualUsername);
    }

    @Test
    void testSetUsername() {
        // Arrange
        String newUsername = "newUser";

        // Act
        user.setUsername(newUsername);

        // Assert
        assertEquals(newUsername, user.getUsername());
    }

    @Test
    void testGetPassword() {
        // Act
        String actualPassword = user.getPassword();

        // Assert
        assertEquals("testPassword", actualPassword);
    }

    @Test
    void testSetPassword() {
        // Arrange
        String newPassword = "newPassword";

        // Act
        user.setPassword(newPassword);

        // Assert
        assertEquals(newPassword, user.getPassword());
    }

    @Test
    void testGetRole() {
        // Act
        Role actualRole = user.getRole();

        // Assert
        assertEquals(role, actualRole);
    }

    @Test
    void testSetRole() {
        // Arrange
        Role newRole = new Role("ROLE_ADMIN");

        // Act
        user.setRole(newRole);

        // Assert
        assertEquals(newRole, user.getRole());
    }

    // New test for getting the email
    @Test
    void testGetEmail() {
        // Act
        String actualEmail = user.getEmail();

        // Assert
        assertEquals("test@example.com", actualEmail);
    }

    // New test for setting the email
    @Test
    void testSetEmail() {
        // Arrange
        String newEmail = "new@example.com";

        // Act
        user.setEmail(newEmail);

        // Assert
        assertEquals(newEmail, user.getEmail());
    }
}

