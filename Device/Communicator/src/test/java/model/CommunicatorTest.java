package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the SimulatedDevice class.
 */
class CommunicatorTest {

    @Test
    void testConstructorAndGetters() {
        // Arrange
        String expectedDeviceId = "device123";
        String expectedSecretKey = "secret123";

        // Act
        Communicator communicator = new Communicator(expectedDeviceId, expectedSecretKey);

        // Assert
        assertEquals(expectedDeviceId, communicator.getDeviceId(), "Device ID should match the expected value.");
        assertEquals(expectedSecretKey, communicator.getSecretKey(), "Secret key should match the expected value.");
        assertNull(communicator.getJwtToken(), "JWT token should be null by default.");
    }

    @Test
    void testSetAndGetJwtToken() {
        // Arrange
        Communicator communicator = new Communicator("device123", "secret123");
        String expectedJwtToken = "jwt-token-123";

        // Act
        communicator.setJwtToken(expectedJwtToken);

        // Assert
        assertEquals(expectedJwtToken, communicator.getJwtToken(), "JWT token should match the expected value.");
    }
}
