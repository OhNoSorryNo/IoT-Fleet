package IoTFleetManagment.test.firmware.model;

import IoTFleetManagement.firmware.model.FirmwareVersion;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class FirmwareVersionTest {

    @Test
    public void testNoArgsConstructor() {
        // Act
        FirmwareVersion firmwareVersion = new FirmwareVersion();

        // Assert
        assertNotNull(firmwareVersion);
        assertNull(firmwareVersion.getId());
        assertNull(firmwareVersion.getTag());
        assertNull(firmwareVersion.getImageName());
        assertNull(firmwareVersion.getUrl());
        assertNull(firmwareVersion.getReleaseDate());
    }

    @Test
    public void testAllArgsConstructor() {
        // Arrange
        String tag = "v1.0";
        String imageName = "firmware-image-1.0.0";
        String url = "https://example.com/firmware/v1.0.0";
        LocalDate releaseDate = LocalDate.of(2024, 12, 16);

        // Act
        FirmwareVersion firmwareVersion = new FirmwareVersion(tag, imageName, url, releaseDate);

        // Assert
        assertNotNull(firmwareVersion);
        assertNull(firmwareVersion.getId()); // ID is not set by constructor
        assertEquals(tag, firmwareVersion.getTag());
        assertEquals(imageName, firmwareVersion.getImageName());
        assertEquals(url, firmwareVersion.getUrl());
        assertEquals(releaseDate, firmwareVersion.getReleaseDate());
    }

    @Test
    public void testSettersAndGetters() {
        // Arrange
        FirmwareVersion firmwareVersion = new FirmwareVersion();

        Long id = 1L;
        String tag = "v1.0";
        String imageName = "firmware-image-1.0.0";
        String url = "https://example.com/firmware/v1.0.0";
        LocalDate releaseDate = LocalDate.of(2024, 12, 16);

        // Act
        firmwareVersion.setId(id);
        firmwareVersion.setTag(tag);
        firmwareVersion.setImageName(imageName);
        firmwareVersion.setUrl(url);
        firmwareVersion.setReleaseDate(releaseDate);

        // Assert
        assertEquals(id, firmwareVersion.getId());
        assertEquals(tag, firmwareVersion.getTag());
        assertEquals(imageName, firmwareVersion.getImageName());
        assertEquals(url, firmwareVersion.getUrl());
        assertEquals(releaseDate, firmwareVersion.getReleaseDate());
    }

    @Test
    public void testNotEquals() {
        // Arrange
        FirmwareVersion firmware1 = new FirmwareVersion("v1.0", "firmware-image-1.0.0", "https://example.com/firmware/v1.0.0", LocalDate.of(2024, 12, 16));
        FirmwareVersion firmware2 = new FirmwareVersion("v2.0", "firmware-image-2.0.0", "https://example.com/firmware/v2.0.0", LocalDate.of(2025, 1, 1));

        firmware1.setId(1L);
        firmware2.setId(2L);

        // Act & Assert
        assertNotEquals(firmware1, firmware2);
    }
}

