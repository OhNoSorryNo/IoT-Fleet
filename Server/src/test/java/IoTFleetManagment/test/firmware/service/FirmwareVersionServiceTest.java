package IoTFleetManagment.test.firmware.service;

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
        FirmwareVersion firmwareVersion = new FirmwareVersion(
                "v1.0", "firmware-image-1.0.0", "https://example.com/firmware/v1.0.0", LocalDate.of(2024, 12, 16)
        );
        when(firmwareVersionRepository.save(firmwareVersion)).thenReturn(firmwareVersion);

        // Act
        FirmwareVersion savedFirmware = firmwareVersionService.addFirmwareVersion(firmwareVersion);

        // Assert
        assertNotNull(savedFirmware);
        assertEquals("v1.0", savedFirmware.getTag());
        verify(firmwareVersionRepository, times(1)).save(firmwareVersion);
    }

    @Test
    public void testGetAllFirmwareVersions() {
        // Arrange
        FirmwareVersion firmware1 = new FirmwareVersion(
                "v1.0", "firmware-image-1.0.0", "https://example.com/firmware/v1.0.0", LocalDate.of(2024, 12, 16)
        );
        FirmwareVersion firmware2 = new FirmwareVersion(
                "v2.0", "firmware-image-2.0.0", "https://example.com/firmware/v2.0.0", LocalDate.of(2025, 1, 1)
        );
        when(firmwareVersionRepository.findAll()).thenReturn(Arrays.asList(firmware1, firmware2));

        // Act
        List<FirmwareVersion> firmwareVersions = firmwareVersionService.getAllFirmwareVersions();

        // Assert
        assertNotNull(firmwareVersions);
        assertEquals(2, firmwareVersions.size());
        assertEquals("v1.0", firmwareVersions.get(0).getTag());
        assertEquals("v2.0", firmwareVersions.get(1).getTag());
        verify(firmwareVersionRepository, times(1)).findAll();
    }

    @Test
    public void testGetLatestFirmwareVersion() {
        // Arrange
        FirmwareVersion firmware1 = new FirmwareVersion(
                "v1.0", "firmware-image-1.0.0", "https://example.com/firmware/v1.0.0", LocalDate.of(2024, 12, 16)
        );
        FirmwareVersion firmware2 = new FirmwareVersion(
                "v2.0", "firmware-image-2.0.0", "https://example.com/firmware/v2.0.0", LocalDate.of(2025, 1, 1)
        );
        when(firmwareVersionRepository.findAll()).thenReturn(Arrays.asList(firmware1, firmware2));

        // Act
        FirmwareVersion latestFirmware = firmwareVersionService.getLatestFirmwareVersion();

        // Assert
        assertNotNull(latestFirmware);
        assertEquals("v2.0", latestFirmware.getTag());
        verify(firmwareVersionRepository, times(1)).findAll();
    }

    @Test
    public void testGetLatestFirmwareVersionThrowsExceptionWhenNoFirmware() {
        // Arrange
        when(firmwareVersionRepository.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            firmwareVersionService.getLatestFirmwareVersion();
        });

        assertEquals("No firmware versions available", exception.getMessage());
        verify(firmwareVersionRepository, times(1)).findAll();
    }

}

