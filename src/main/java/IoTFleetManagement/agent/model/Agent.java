package IoTFleetManagement.agent.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Agent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String agentId;

    @Column(nullable = false)
    private String secretKey;

    private LocalDateTime lastSeen;

    private boolean online;

    private String token;

    private String firmwareVersion;

    private int pingFrequency;

    private String agentType;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getAgentId() {
        return agentId;
    }
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }
    public String getSecretKey() {
        return secretKey;
    }
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
    public LocalDateTime getLastSeen() {
        return lastSeen;
    }
    public void setLastSeen(LocalDateTime lastSeen) {
        this.lastSeen = lastSeen;
    }
    public boolean getOnline() {
        return online;
    }
    public void setOnline(boolean online) {
        this.online = online;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public String getFirmwareVersion() {
        return firmwareVersion;
    }
    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }
    public int getPingFrequency() {
        return pingFrequency;
    }
    public void setPingFrequency(int pingFrequency) {
        this.pingFrequency = pingFrequency;
    }
    public String getAgentType() {
        return agentType;
    }
    public void setAgentType(String agentType) {
        this.agentType = agentType;
    }
}
