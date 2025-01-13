package IoTFleetManagement.firmware.controller;

import IoTFleetManagement.firmware.model.FirmwareVersion;
import IoTFleetManagement.firmware.service.FirmwareVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing firmware versions in the IoT Fleet Management system.
 *
 * <p>This controller provides endpoints to perform CRUD operations and retrieve firmware version details.
 * It serves as the interface between the frontend or client applications and the backend services
 * for firmware version management.</p>
 *
 * @see IoTFleetManagement.firmware.service.FirmwareVersionService
 * @see IoTFleetManagement.firmware.model.FirmwareVersion
 */
@RestController
@RequestMapping("/firmware")
public class FirmwareVersionController {

    @Autowired
    private FirmwareVersionService firmwareVersionService;

    /**
     * Adds a new firmware version.
     *
     * @param firmwareVersion the firmware version to be added, provided in the request body
     * @return the added {@link FirmwareVersion} instance with a generated ID
     */
    @PostMapping
    public FirmwareVersion addFirmwareVersion(@RequestBody FirmwareVersion firmwareVersion) {
        return firmwareVersionService.addFirmwareVersion(firmwareVersion);
    }

    /**
     * Retrieves all firmware versions.
     *
     * @return a list of all {@link FirmwareVersion} instances available in the system
     */
    @GetMapping
    public List<FirmwareVersion> getAllFirmwareVersions() {
        return firmwareVersionService.getAllFirmwareVersions();
    }

    /**
     * Retrieves the latest firmware version.
     *
     * <p>The latest firmware version is determined based on the release date.</p>
     *
     * @return the latest {@link FirmwareVersion} instance
     */
    @GetMapping("/latest")
    public FirmwareVersion getLatestFirmwareVersion() {
        return firmwareVersionService.getLatestFirmwareVersion();
    }
}
