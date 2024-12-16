package IoTFleetManagement.firmware.controller;

import IoTFleetManagement.firmware.model.FirmwareVersion;
import IoTFleetManagement.firmware.service.FirmwareVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/firmware")
public class FirmwareVersionController {

    @Autowired
    private FirmwareVersionService firmwareVersionService;

    // Add a new firmware version
    @PostMapping
    public FirmwareVersion addFirmwareVersion(@RequestBody FirmwareVersion firmwareVersion) {
        return firmwareVersionService.addFirmwareVersion(firmwareVersion);
    }

    // Get all firmware versions
    @GetMapping
    public List<FirmwareVersion> getAllFirmwareVersions() {
        return firmwareVersionService.getAllFirmwareVersions();
    }

    // Get the latest firmware version
    @GetMapping("/latest")
    public FirmwareVersion getLatestFirmwareVersion() {
        return firmwareVersionService.getLatestFirmwareVersion();
    }
}
