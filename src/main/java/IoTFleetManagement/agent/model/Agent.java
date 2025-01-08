package IoTFleetManagement.agent.model;

import IoTFleetManagement.firmware.model.FirmwareVersion;
import IoTFleetManagement.user.model.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entity class representing an IoT Agent in the IoT fleet management system.
 * <p>
 * This class is mapped to a database table to store information about IoT agents,
 * including their identification, authentication credentials, status, and other relevant details.
 * The agents can be manually added to the database, and their status can be retrieved or updated.
 *
 * @author Lara
 * @author Jasmin1707
 */
@Entity
public class Agent {
    /**
     * Unique identifier for each agent, generated automatically by the database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique identifier assigned to each agent.
     */
    @Column(unique = true, nullable = false)
    private String agentId;

    /**
     * Secret key used for agent authentication. This key is stored in a hashed format.
     */
    @Column(nullable = false)
    private String secretKey;

    /**
     * Timestamp of the last time the agent was seen by the system.
     */
    private LocalDateTime lastSeen;

    /**
     * Indicates whether the agent is currently online. Defaults to {@code false}.
     */
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private boolean online;

    /**
     * JWT token issued to the agent for secure communication.
     */
    @Column(nullable = false)
    private String token;

    /**
     * Firmware version currently installed on the agent.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "firmware_version_id", referencedColumnName = "id")
    private FirmwareVersion firmwareVersion;

    /**
     * Frequency in milliseconds at which the agent sends heartbeat pings to the server.
     */
    private int pingFrequency;

    /**
     * Type of the agent, e.g., sensor, actuator, etc.
     */
    private String agentType;

    /**
     * The user who owns this agent.
     * <p>
     * This field establishes a many-to-one relationship between the `Agent` and `User` entities.
     * Each agent is associated with a single user, but a user can have multiple agents.
     * </p>
     * <ul>
     *   <li>The relationship is eagerly fetched, meaning the associated `User` entity is loaded immediately whenever an `Agent` is fetched.</li>
     *   <li>The `@JoinColumn` annotation specifies that this relationship is mapped using the `user_id` column in the `Agent` table.</li>
     *   <li>The `@JsonBackReference` annotation prevents cyclic references during serialization by marking this side as the "back" reference.</li>
     * </ul>
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    /**
     * Gets the unique identifier for the agent.
     *
     * @return the unique identifier of the agent
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the unique identifier for the agent.
     *
     * @param id the unique identifier to be set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the unique agent ID.
     *
     * @return the agent ID
     */
    public String getAgentId() {
        return agentId;
    }

    /**
     * Sets the unique agent ID.
     *
     * @param agentId the agent ID to be set
     */
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    /**
     * Gets the secret key for authentication.
     *
     * @return the secret key
     */
    public String getSecretKey() {
        return secretKey;
    }

    /**
     * Sets the secret key for authentication.
     *
     * @param secretKey the secret key to be set
     */
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    /**
     * Gets the timestamp of when the agent was last seen.
     *
     * @return the last seen timestamp
     */
    public LocalDateTime getLastSeen() {
        return lastSeen;
    }

    /**
     * Sets the timestamp of when the agent was last seen.
     *
     * @param lastSeen the last seen timestamp to be set
     */
    public void setLastSeen(LocalDateTime lastSeen) {
        this.lastSeen = lastSeen;
    }

    /**
     * Checks if the agent is online.
     *
     * @return {@code true} if the agent is online, {@code false} otherwise
     */
    public boolean isOnline() {
        return online;
    }

    /**
     * Sets the online status of the agent.
     *
     * @param online the online status to be set
     */
    public void setOnline(boolean online) {
        this.online = online;
    }

    /**
     * Gets the JWT token issued to the agent.
     *
     * @return the JWT token
     */
    public String getToken() {
        return token;
    }

    /**
     * Sets the JWT token issued to the agent.
     *
     * @param token the JWT token to be set
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Gets the firmware version installed on the agent.
     *
     * @return the firmware version
     */
    public FirmwareVersion getFirmwareVersion() {
        return firmwareVersion;
    }

    /**
     * Sets the firmware version installed on the agent.
     *
     * @param firmwareVersion the firmware version to be set
     */
    public void setFirmwareVersion(FirmwareVersion firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    /**
     * Gets the ping frequency of the agent.
     *
     * @return the ping frequency in milliseconds
     */
    public int getPingFrequency() {
        return pingFrequency;
    }

    /**
     * Sets the ping frequency of the agent.
     *
     * @param pingFrequency the ping frequency to be set in milliseconds
     */
    public void setPingFrequency(int pingFrequency) {
        this.pingFrequency = pingFrequency;
    }

    /**
     * Gets the type of the agent.
     *
     * @return the agent type
     */
    public String getAgentType() {
        return agentType;
    }

    /**
     * Sets the type of the agent.
     *
     * @param agentType the type of the agent to be set
     */
    public void setAgentType(String agentType) {
        this.agentType = agentType;
    }

    /**
     * Gets the user. to whom the device belongs.
     *
     * @return The user object
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the owner of the device.
     *
     * @param user The owner of the device.
     */
    public void setUser(User user) {
        this.user = user;
    }
}
