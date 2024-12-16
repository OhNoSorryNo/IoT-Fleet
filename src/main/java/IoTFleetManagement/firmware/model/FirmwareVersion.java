package IoTFleetManagement.firmware.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "firmware_versions")
public class FirmwareVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String version;

    @Column(nullable = false)
    private String imageName;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private LocalDate releaseDate;

    // Constructors
    public FirmwareVersion() {}

    public FirmwareVersion(String version, String imageName, String url, LocalDate releaseDate) {
        this.version = version;
        this.imageName = imageName;
        this.url = url;
        this.releaseDate = releaseDate;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }
}
