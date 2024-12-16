package IoTFleetManagement.firmware.service;

import IoTFleetManagement.firmware.model.FirmwareVersion;
import IoTFleetManagement.firmware.repository.FirmwareVersionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for managing firmware versions in the IoT Fleet Management system.
 *
 * <p>This service provides business logic for adding, retrieving, and managing firmware versions.
 * It interacts with the {@link FirmwareVersionRepository} to persist and query firmware data.</p>
 * such as:
 * Adding a new firmware version to the system.
 * Retrieving all available firmware versions.
 * Identifying and return the latest firmware version based on the release date.
 *
 * @see IoTFleetManagement.firmware.model.FirmwareVersion
 * @see IoTFleetManagement.firmware.repository.FirmwareVersionRepository
 * @see IoTFleetManagement.firmware.controller.FirmwareVersionController
 */
@Service
public class FirmwareVersionService {

    @Autowired
    private FirmwareVersionRepository firmwareVersionRepository;

    /**
     * Adds a new firmware version to the system.
     *
     * <p>This method saves the provided firmware version instance in the database.</p>
     *
     * @param firmwareVersion the firmware version to be added
     * @return the saved {@link FirmwareVersion} instance with a generated ID
     */
    public FirmwareVersion addFirmwareVersion(FirmwareVersion firmwareVersion) {
        return firmwareVersionRepository.save(firmwareVersion);
    }

    /**
     * Retrieves all available firmware versions from the database.
     *
     * <p>This method fetches all firmware versions stored in the system and returns them as a list.</p>
     *
     * @return a list of all {@link FirmwareVersion} instances
     */
    public List<FirmwareVersion> getAllFirmwareVersions() {
        return firmwareVersionRepository.findAll();
    }

    /**
     * Retrieves the latest firmware version based on the release date.
     *
     * <p>This method identifies the firmware version with the most recent release date from the database.
     * If no firmware versions are available, it throws a runtime exception.</p>
     *
     * @return the latest {@link FirmwareVersion} instance
     * @throws RuntimeException if no firmware versions are available in the system
     */
    public FirmwareVersion getLatestFirmwareVersion() {
        return firmwareVersionRepository.findAll()
                .stream()
                .max((f1, f2) -> f1.getReleaseDate().compareTo(f2.getReleaseDate()))
                .orElseThrow(() -> new RuntimeException("No firmware versions available"));
    }
}
