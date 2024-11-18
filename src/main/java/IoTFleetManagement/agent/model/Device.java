package IoTFleetManagement.agent.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String deviceId;

    @Column(nullable = false)
    private String secretKey;

    private LocalDateTime lastSeen;

    private String status;

    private String token;

    private String firmwareVersion;

    private int pingFrequency;

    private String deviceType;
}
