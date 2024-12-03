package IoTFleetmanagement.test.device.model;

import IoTFleetManagement.device.model.SimulatedDevice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SimulatedDeviceTest {

    private SimulatedDevice simulatedDevice;

    @BeforeEach
    void setUp() {
        simulatedDevice = new SimulatedDevice("testDeviceId", "testSecretKey");
    }

    @Test
    public void testSimulatedDeviceCreation() {
        // Assert
        assertNotNull(simulatedDevice);
        assertEquals("testDeviceId", simulatedDevice.getDeviceId());
        assertEquals("testSecretKey", simulatedDevice.getSecretKey());
    }

    @Test
    public void testSetJwtToken() {
        // Act
        simulatedDevice.setJwtToken("testJwtToken");

        // Assert
        assertEquals("testJwtToken", simulatedDevice.getJwtToken());
    }
}
