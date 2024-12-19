package IoTFleetmanagement.test.firmware.service;

import IoTFleetManagement.firmware.model.FirmwareVersion;
import IoTFleetManagement.firmware.repository.FirmwareVersionRepository;
import IoTFleetManagement.firmware.service.FirmwareVersionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FirmwareVersionServiceTest {

    @Mock
    private FirmwareVersionRepository firmwareVersionRepository;

    @InjectMocks
    private FirmwareVersionService firmwareVersionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize mocks
    }

    @Test
    public void testAddFirmwareVersion() {
        // Arrange
        FirmwareVersion firmwareVersion = new FirmwareVersion("v1.0", "firmware_v1.0", "https://dockerhub.com/image_v1.0", LocalDate.of(2024, 6, 1));
        when(firmwareVersionRepository.save(firmwareVersion)).thenReturn(firmwareVersion);

        // Act
        FirmwareVersion result = firmwareVersionService.addFirmwareVersion(firmwareVersion);

        // Assert
        assertNotNull(result);
        assertEquals("v1.0", result.getVersion());
        assertEquals("firmware_v1.0", result.getImageName());
        verify(firmwareVersionRepository, times(1)).save(firmwareVersion); // Ensure save() was called once
    }

    @Test
    public void testGetAllFirmwareVersions() {
        // Arrange
        FirmwareVersion version1 = new FirmwareVersion("v1.0", "firmware_v1.0", "https://dockerhub.com/image_v1.0", LocalDate.of(2024, 6, 1));
        FirmwareVersion version2 = new FirmwareVersion("v2.0", "firmware_v2.0", "https://dockerhub.com/image_v2.0", LocalDate.of(2024, 7, 1));
        when(firmwareVersionRepository.findAll()).thenReturn(Arrays.asList(version1, version2));

        // Act
        List<FirmwareVersion> result = firmwareVersionService.getAllFirmwareVersions();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("v1.0", result.get(0).getVersion());
        assertEquals("v2.0", result.get(1).getVersion());
        verify(firmwareVersionRepository, times(1)).findAll(); // Ensure findAll() was called once
    }

    @Test
    public void testGetLatestFirmwareVersion() {
        // Arrange
        FirmwareVersion version1 = new FirmwareVersion("v1.0", "firmware_v1.0", "https://dockerhub.com/image_v1.0", LocalDate.of(2024, 6, 1));
        FirmwareVersion version2 = new FirmwareVersion("v2.0", "firmware_v2.0", "https://dockerhub.com/image_v2.0", LocalDate.of(2024, 7, 1));
        when(firmwareVersionRepository.findAll()).thenReturn(Arrays.asList(version1, version2));

        // Act
        FirmwareVersion result = firmwareVersionService.getLatestFirmwareVersion();

        // Assert
        assertNotNull(result);
        assertEquals("v2.0", result.getVersion());
        assertEquals("firmware_v2.0", result.getImageName());
        verify(firmwareVersionRepository, times(1)).findAll(); // Ensure findAll() was called once
    }

    @Test
    public void testGetLatestFirmwareVersionThrowsExceptionWhenNoFirmware() {
        // Arrange
        when(firmwareVersionRepository.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> firmwareVersionService.getLatestFirmwareVersion());
        assertEquals("No firmware versions available", exception.getMessage());
        verify(firmwareVersionRepository, times(1)).findAll(); // Ensure findAll() was called once
    }
}
