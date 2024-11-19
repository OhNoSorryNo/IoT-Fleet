package IoTFleetmanagement.test.user.model;


import IoTFleetManagement.user.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoleTest {

    private Role role;

    @BeforeEach
    void setUp() {
        role = new Role("ROLE_ADMIN");
    }

    @Test
    void testGetId() {
        // Arrange
        Long expectedId = 1L;
        role.setId(expectedId);

        // Act
        Long actualId = role.getId();

        // Assert
        assertEquals(expectedId, actualId);
    }

    @Test
    void testGetName() {
        // Act
        String actualName = role.getName();

        // Assert
        assertEquals("ROLE_ADMIN", actualName);
    }

    @Test
    void testSetName() {
        // Arrange
        String newName = "ROLE_USER";

        // Act
        role.setName(newName);

        // Assert
        assertEquals(newName, role.getName());
    }
}
