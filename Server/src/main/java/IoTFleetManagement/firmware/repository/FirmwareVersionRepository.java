package IoTFleetManagement.firmware.repository;

import IoTFleetManagement.firmware.model.FirmwareVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

/**
 * Repository interface for managing firmware versions in the IoT Fleet Management system.
 *
 * <p>This interface provides data access methods for {@link FirmwareVersion} entities. It extends
 * the {@link JpaRepository}, offering standard CRUD operations and custom queries for specific
 * use cases.</p>
 *
 * @see IoTFleetManagement.firmware.model.FirmwareVersion
 */
public interface FirmwareVersionRepository extends JpaRepository<FirmwareVersion, Long> {
    // Find a firmware tag by its tag number
    Optional<FirmwareVersion> findByTag(String tag);

    // Retrieve the latest firmware version based on release date
    @Query("SELECT f FROM FirmwareVersion f ORDER BY f.releaseDate DESC LIMIT 1")
    Optional<FirmwareVersion> findLatestFirmware();
}
