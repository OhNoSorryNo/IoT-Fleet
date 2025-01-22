package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the SimulatedDevice class.
 */
class SimulatedDeviceTest {

    @Test
    void testConstructorAndGetters() {
        // Arrange
        String expectedDeviceId = "device123";
        String expectedSecretKey = "secret123";

        // Act
        SimulatedDevice simulatedDevice = new SimulatedDevice(expectedDeviceId, expectedSecretKey);

        // Assert
        assertEquals(expectedDeviceId, simulatedDevice.getDeviceId(), "Device ID should match the expected value.");
        assertEquals(expectedSecretKey, simulatedDevice.getSecretKey(), "Secret key should match the expected value.");
        assertNull(simulatedDevice.getJwtToken(), "JWT token should be null by default.");
    }

    @Test
    void testSetAndGetJwtToken() {
        // Arrange
        SimulatedDevice simulatedDevice = new SimulatedDevice("device123", "secret123");
        String expectedJwtToken = "jwt-token-123";

        // Act
        simulatedDevice.setJwtToken(expectedJwtToken);

        // Assert
        assertEquals(expectedJwtToken, simulatedDevice.getJwtToken(), "JWT token should match the expected value.");
    }
}
