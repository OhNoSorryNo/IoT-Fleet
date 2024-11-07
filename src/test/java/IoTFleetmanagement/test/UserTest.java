package IoTFleetmanagement.test;


import IoTFleetManagement.model.Role;
import IoTFleetManagement.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserTest {

    private User user;
    private Role role;

    @BeforeEach
    void setUp() {
        role = new Role("ROLE_USER");
        user = new User("testUser", "testPassword", role);
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
}
