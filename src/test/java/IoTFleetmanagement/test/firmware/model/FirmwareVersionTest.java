package IoTFleetmanagement.test.firmware.model;

import IoTFleetManagement.firmware.model.FirmwareVersion;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class FirmwareVersionTest {

    @Test
    public void testDefaultConstructor() {
        // Create an instance using the default constructor
        FirmwareVersion firmwareVersion = new FirmwareVersion();

        // Assert default values
        assertNull(firmwareVersion.getId());
        assertNull(firmwareVersion.getVersion());
        assertNull(firmwareVersion.getImageName());
        assertNull(firmwareVersion.getUrl());
        assertNull(firmwareVersion.getReleaseDate());
    }

    @Test
    public void testParameterizedConstructor() {
        // Create an instance using the parameterized constructor
        FirmwareVersion firmwareVersion = new FirmwareVersion(
                "v1.0",
                "firmware_v1.0",
                "https://dockerhub.com/image_v1.0",
                LocalDate.of(2024, 6, 1)
        );

        // Assert values
        assertNull(firmwareVersion.getId()); // ID should still be null
        assertEquals("v1.0", firmwareVersion.getVersion());
        assertEquals("firmware_v1.0", firmwareVersion.getImageName());
        assertEquals("https://dockerhub.com/image_v1.0", firmwareVersion.getUrl());
        assertEquals(LocalDate.of(2024, 6, 1), firmwareVersion.getReleaseDate());
    }

    @Test
    public void testSettersAndGetters() {
        // Create an instance
        FirmwareVersion firmwareVersion = new FirmwareVersion();

        // Set values
        firmwareVersion.setId(1L);
        firmwareVersion.setVersion("v1.0");
        firmwareVersion.setImageName("firmware_v1.0");
        firmwareVersion.setUrl("https://dockerhub.com/image_v1.0");
        firmwareVersion.setReleaseDate(LocalDate.of(2024, 6, 1));

        // Assert values
        assertEquals(1L, firmwareVersion.getId());
        assertEquals("v1.0", firmwareVersion.getVersion());
        assertEquals("firmware_v1.0", firmwareVersion.getImageName());
        assertEquals("https://dockerhub.com/image_v1.0", firmwareVersion.getUrl());
        assertEquals(LocalDate.of(2024, 6, 1), firmwareVersion.getReleaseDate());
    }
}
