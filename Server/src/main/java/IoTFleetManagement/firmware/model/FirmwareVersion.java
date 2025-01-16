package IoTFleetManagement.firmware.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Represents a firmware version in the IoT Fleet Management system.
 *
 * <p>This entity models a firmware version that can be assigned to IoT devices.
 * It includes details such as the version, image name, download URL, and release date.</p>
 *
 * <p>The `FirmwareVersion` entity is mapped to the `firmware_versions` table in the database.</p>
 *
 * Database Mapping
 * <ul>
 *     id: Primary key, auto-generated
 *     version: Firmware version identifier (e.g., "v1.0.0")
 *     imageName: Name of the firmware image
 *     url: Download URL for the firmware image
 *     releaseDate: Release date of the firmware version
 * </ul>
 *
 * {@code
 * FirmwareVersion firmware = new FirmwareVersion(
 *     "v1.0.0",
 *     "firmware-image-1.0.0",
 *     "https://example.com/firmware/v1.0.0",
 *     LocalDate.of(2024, 12, 16)
 * );
 * }
 */
@Entity
@Table(name = "firmware_versions")
public class FirmwareVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tag;


    @Column(nullable = false)
    private String imageName;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private LocalDate releaseDate;

    /**
     * Default no-argument constructor.
     */
    public FirmwareVersion() {}

    /**
     * Constructs a new FirmwareVersion with the specified details.
     *
     * @param tag     the firmware version identifier
     * @param imageName   the name of the firmware image
     * @param url         the download URL for the firmware
     * @param releaseDate the release date of the firmware
     */
    public FirmwareVersion(String tag, String imageName, String url, LocalDate releaseDate) {
        this.tag = tag;
        this.imageName = imageName;
        this.url = url;
        this.releaseDate = releaseDate;
    }

    /**
     * Returns the unique identifier of the firmware version.
     *
     * @return the firmware version ID
     */
    public Long getId() { return id; }

    /**
     * Sets the unique identifier of the firmware version.
     *
     * @param id the firmware version ID to set
     */
    public void setId(Long id) { this.id = id; }

    /**
     * Returns the version identifier of the firmware.
     *
     * @return the firmware version
     */
    public String getTag() { return tag; }

    /**
     * Sets the tag identifier of the firmware.
     *
     * @param tag the firmware tag to set
     */
    public void setTag(String tag) { this.tag = tag; }

    /**
     * Returns the name of the firmware image.
     *
     * @return the firmware image name
     */
    public String getImageName() { return imageName; }

    /**
     * Sets the name of the firmware image.
     *
     * @param imageName the firmware image name to set
     */
    public void setImageName(String imageName) { this.imageName = imageName; }

    /**
     * Returns the download URL for the firmware image.
     *
     * @return the firmware URL
     */
    public String getUrl() { return url; }

    /**
     * Sets the download URL for the firmware image.
     *
     * @param url the firmware URL to set
     */
    public void setUrl(String url) { this.url = url; }

    /**
     * Returns the release date of the firmware version.
     *
     * @return the firmware release date
     */
    public LocalDate getReleaseDate() { return releaseDate; }

    /**
     * Sets the release date of the firmware version.
     *
     * @param releaseDate the firmware release date to set
     */
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }


}
