package IoTFleetManagement.firmware.repository;

import IoTFleetManagement.firmware.model.FirmwareVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface FirmwareVersionRepository extends JpaRepository<FirmwareVersion, Long> {
    // Find a firmware version by its version number
    Optional<FirmwareVersion> findByVersion(String version);

    // Retrieve the latest firmware version based on release date
    @Query("SELECT f FROM FirmwareVersion f ORDER BY f.releaseDate DESC LIMIT 1")
    Optional<FirmwareVersion> findLatestFirmware();
}
