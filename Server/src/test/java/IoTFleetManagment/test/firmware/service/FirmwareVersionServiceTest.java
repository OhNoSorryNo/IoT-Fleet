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

}

