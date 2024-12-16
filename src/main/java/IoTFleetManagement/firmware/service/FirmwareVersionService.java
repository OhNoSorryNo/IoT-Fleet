package IoTFleetManagement.firmware.service;

import IoTFleetManagement.firmware.model.FirmwareVersion;
import IoTFleetManagement.firmware.repository.FirmwareVersionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FirmwareVersionService {

    @Autowired
    private FirmwareVersionRepository firmwareVersionRepository;

    // Add a new firmware version
    public FirmwareVersion addFirmwareVersion(FirmwareVersion firmwareVersion) {
        return firmwareVersionRepository.save(firmwareVersion);
    }

    // Retrieve all firmware versions
    public List<FirmwareVersion> getAllFirmwareVersions() {
        return firmwareVersionRepository.findAll();
    }

    // Retrieve the latest firmware version
    public FirmwareVersion getLatestFirmwareVersion() {
        return firmwareVersionRepository.findAll()
                .stream()
                .max((f1, f2) -> f1.getReleaseDate().compareTo(f2.getReleaseDate()))
                .orElseThrow(() -> new RuntimeException("No firmware versions available"));
    }
}
