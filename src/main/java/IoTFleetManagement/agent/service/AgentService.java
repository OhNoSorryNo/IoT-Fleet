package IoTFleetManagement.agent.service;

import IoTFleetManagement.firmware.model.FirmwareVersion;
import IoTFleetManagement.firmware.repository.FirmwareVersionRepository;
import IoTFleetManagement.user.model.User;
import IoTFleetManagement.agent.model.Agent;
import IoTFleetManagement.user.repository.UserRepository;
import IoTFleetManagement.agent.repository.AgentRepository;
import IoTFleetManagement.common.exceptions.AlreadyExistsException;
import IoTFleetManagement.security.config.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing IoT Agents in the fleet management system.
 * <p>
 * This class provides the business logic for managing agents, including adding new agents,
 * retrieving agent information, checking agent status, updating status, and assigning agents to users.
 * </p>
 *
 * @author Lara
 * @author Jasmin1707
 * @author Miriam
 */
@Service
public class AgentService {

    @Autowired
    private final AgentRepository agentRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private FirmwareVersionRepository firmwareVersionRepository;

    private static final Logger log = LoggerFactory.getLogger(AgentService.class);

    /**
     * Constructor to initialize the AgentService with required dependencies.
     *
     * @param agentRepository the repository for managing CRUD operations on agents
     * @param userRepository  the repository for managing user-related operations
     * @param jwtUtil         utility for managing JWT tokens
     */
    @Autowired
    public AgentService(AgentRepository agentRepository, UserRepository userRepository, JwtUtil jwtUtil) {
        this.agentRepository = agentRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = new BCryptPasswordEncoder(); // Initialize once
    }

    /**
     * Retrieves a list of all agents in the system.
     *
     * @return a list of all agents
     */
    public List<Agent> getAllAgents() {
        log.info("Fetching all agents from the repository");
        return agentRepository.findAll();
    }

    /**
     * Retrieves the online status of a specific agent.
     *
     * @param agentId the unique identifier of the agent
     * @return {@code true} if the agent is online, {@code false} otherwise
     * @throws ChangeSetPersister.NotFoundException if the agent is not found
     */
    public boolean getAgentStatus(String agentId) throws ChangeSetPersister.NotFoundException {
        log.info("Retrieving status for agent with ID: {}", agentId);
        return agentRepository.findByAgentId(agentId)
                .map(Agent::isOnline)
                .orElseThrow(() -> {
                    log.warn("Agent not found with ID: {}", agentId);
                    return new ChangeSetPersister.NotFoundException();
                });
    }

    /**
     * Adds a new agent to the system.
     * <p>
     * This method validates the input, hashes the secret key, generates a JWT token,
     * and saves the agent to the database.
     * </p>
     *
     * @param agent the agent to be added
     * @return the saved agent with its assigned ID and token
     * @throws IllegalArgumentException if the agent ID is null or blank
     * @throws AlreadyExistsException   if an agent with the same agent ID already exists
     */
    public Agent addAgent(Agent agent) {
        log.info("Attempting to add a new agent with ID: {}", agent.getAgentId());
        // Validation: Check if the agentId is null or blank
        if (agent.getAgentId() == null || agent.getAgentId().isBlank()) {
            log.error("Invalid agentId: agentId cannot be null or blank");
            throw new IllegalArgumentException("agentId cannot be null or blank");
        }

        // Validation: Check if an agent with the same agentId already exists
        if (agentRepository.findByAgentId(agent.getAgentId()).isPresent()) {
            log.warn("Agent already exists with ID: {}", agent.getAgentId());
            throw new AlreadyExistsException("Agent with this agentId already exists");
        }

        // Hash the secret key before saving
        agent.setSecretKey(passwordEncoder.encode(agent.getSecretKey()));
        log.debug("Secret key for agent {} has been hashed", agent.getAgentId());

        // Generate JWT token
        String token = JwtUtil.generateToken(agent.getAgentId());
        agent.setToken(token);
        log.debug("Generated JWT token for agent: {}", agent.getAgentId());

        Agent savedAgent = agentRepository.save(agent);
        log.info("Agent added successfully with ID: {}", savedAgent.getAgentId());
        return savedAgent;
    }

    /**
     * Checks whether an agent exists by its unique agent ID.
     *
     * @param agentId the unique identifier of the agent
     * @return {@code true} if the agent exists, {@code false} otherwise
     */
    public boolean agentExists(String agentId) {
        log.info("Checking existence of agent with ID: {}", agentId);
        return agentRepository.findByAgentId(agentId).isPresent();
    }

    /**
     * Updates the online status of a specific agent.
     * <p>
     * The method also updates the `lastSeen` timestamp with the current time.
     * </p>
     *
     * @param agentId  the unique identifier of the agent
     * @param isOnline the new online status of the agent
     * @throws ChangeSetPersister.NotFoundException if the agent is not found
     */
    public void updateAgentStatus(String agentId, boolean isOnline) throws ChangeSetPersister.NotFoundException {
        log.info("Updating status for agent with ID: {} to online: {}", agentId, isOnline);
        // Retrieve the agent from the database
        Agent agent = agentRepository.findByAgentId(agentId)
                .orElseThrow(() -> {
                    log.warn("Agent not found with ID: {}", agentId);
                    return new ChangeSetPersister.NotFoundException();
                });

        // Update the online status
        agent.setOnline(isOnline);

        // Set the lastSeen timestamp to the current time
        agent.setLastSeen(LocalDateTime.now());
        log.debug("Updated lastSeen timestamp for agent: {} to current time", agentId);

        // Save the changes to the database
        agentRepository.save(agent);
        log.info("Status updated successfully for agent: {}", agentId);
    }

    /**
     * Validates a JWT token against an agent ID.
     *
     * @param token   the JWT token to validate
     * @param agentId the agent ID the token is associated with
     * @return {@code true} if the token is valid, {@code false} otherwise
     */
    public boolean validateToken(String token, String agentId) {
        log.info("Validating token for agent with ID: {}", agentId);
        return jwtUtil.validateToken(token, agentId);
    }

    // Check the status of all devices every 30 seconds
    @Scheduled(fixedRate = 10000) // Every 30 seconds
    public void checkAllDevicesStatus() {
        log.info("Checking status of all agents to identify offline devices");
        // Calculate the threshold time for devices to be considered offline
        LocalDateTime thresholdTime = LocalDateTime.now().minusSeconds(20); // 60 seconds

        // Find all devices that have not sent a heartbeat before the threshold time and are still marked as online
        List<Agent> agents = agentRepository.findByLastSeenBeforeAndOnline(thresholdTime, true);

        for (Agent agent : agents) {
            // Set the device status to offline
            agent.setOnline(false);
            agentRepository.save(agent);
            log.info("Agent {} has been set to offline due to no heartbeat received in time.", agent.getAgentId());
        }
    }

    /**
     * Authenticates an agent using its agentId and secretKey.
     *
     * @param agentId   the unique identifier of the agent
     * @param secretKey the secret key of the agent
     * @return the authenticated agent
     * @throws ChangeSetPersister.NotFoundException if the agent is not found
     * @throws AuthenticationException if the secret key is invalid
     */
    public Agent authenticate(String agentId, String secretKey) throws ChangeSetPersister.NotFoundException, AuthenticationException {
        log.info("Authenticating agent with ID: {}", agentId);
        Agent agent = agentRepository.findByAgentId(agentId)
                .orElseThrow(() -> {
                    log.warn("Agent not found with ID: {}", agentId);
                    return new ChangeSetPersister.NotFoundException();
                });

        if (passwordEncoder.matches(secretKey, agent.getSecretKey())) {
            log.info("Authentication successful for agent: {}", agentId);
            return agent;
        } else {
            log.warn("Authentication failed for agent: {} - invalid secret key", agentId);
            throw new AuthenticationException("Invalid secret key");
        }
    }

    /**
     * Assigns an agent to a user by their respective IDs.
     *
     * @param agentId the unique identifier of the agent
     * @param userId  the unique identifier of the user
     * @return the updated agent with the assigned user
     * @throws RuntimeException if the agent or user is not found
     */
    public Agent assignAgentToUser(Long agentId, Long userId) {
        log.info("Assigning agent with ID: {} to user with ID: {}", agentId, userId);
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> {
                    log.warn("Agent not found with ID: {}", agentId);
                    return new RuntimeException("Agent not found");
                });
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", userId);
                    return new RuntimeException("User not found");
                });

        agent.setUser(user);
        Agent savedAgent = agentRepository.save(agent);
        log.info("Successfully assigned user with ID: {} to agent with ID: {}", userId, agentId);
        return savedAgent;
    }

    /**
     * Registers an agent to a user based on a secret key.
     * <p>
     * This method verifies the secret key, ensures the agent is not already registered
     * to another user, and assigns the agent to the specified user.
     * </p>
     *
     * @param secretKey the secret key of the agent
     * @param user      the user to assign the agent to
     * @return the updated agent with the assigned user
     * @throws RuntimeException if the secret key is invalid or the agent is already registered to another user
     */
    public Agent registerAgentToUser(String secretKey, User user) {
        log.info("Registering agent for user: {} with provided secret key", user.getUsername());
        // Getting all the agents
        List<Agent> agents = agentRepository.findAll();
        // Find the agent by its secret key
        Agent agent = agents.stream()
                .filter(a -> passwordEncoder.matches(secretKey, a.getSecretKey()))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Invalid secret key provided for user: {}", user.getUsername());
                    return new RuntimeException("Invalid secret key");
                });

        // Check if the agent is already assigned to another user
        if (agent.getUser() != null) {
            log.warn("Agent with ID: {} is already registered to another user", agent.getAgentId());
            throw new RuntimeException("This agent is already registered to another user");
        }

        // Assign the agent to the user
        agent.setUser(user);
        Agent savedAgent = agentRepository.save(agent);
        log.info("Successfully assigned agent with ID: {} to user: {}", agent.getAgentId(), user.getUsername());
        return savedAgent;
    }

    /**
     * Retrieves an agent from the database using its unique agent ID.
     *
     * @param agentId agentId the unique identifier of the agent to retrieve
     * @return an {@link Optional} containing the {@link Agent} if found, or an empty {@link Optional} otherwise
     */
    public Optional<Agent> getAgentByAgentId(String agentId) {
        return agentRepository.findByAgentId(agentId);
    }

    /**
     * Removes the association between an agent and its user by setting the user to null.
     *
     * @param agent the agent to be updated
     * @return the updated agent with the user set to null
     */
    public Agent removeUserFromAgent(Agent agent) {
        log.info("Removing user from agent with ID: {}", agent.getAgentId());

        agent.setUser(null);

        // Save the updated agent in the repository
        Agent updatedAgent = agentRepository.save(agent);
        log.info("Successfully removed user from agent with ID: {}", agent.getAgentId());

        return updatedAgent;
    }

    /**
     * Saves the given agent to the database.
     *
     * @param agent the agent to be saved
     * @return the saved agent
     */
    public Agent saveAgent(Agent agent) {
        log.info("Saving agent with ID: {}", agent.getAgentId());
        return agentRepository.save(agent);
    }

    /**
     * Assigns a firmware version to an agent by updating the agent's firmware version.
     *
     * <p>This method retrieves an agent and a firmware version by their respective IDs.
     * If both exist, it assigns the specified firmware version to the agent and saves the updated
     * agent back to the database.</p>
     *
     * @param agentId    the unique identifier of the agent to which the firmware version will be assigned
     * @param firmwareId the unique identifier of the firmware version to assign to the agent
     * @return the updated {@link Agent} entity after the firmware assignment
     */
    public Agent assignFirmwareToAgent(Long agentId, Long firmwareId) {
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));

        FirmwareVersion firmwareVersion = firmwareVersionRepository.findById(firmwareId)
                .orElseThrow(() -> new RuntimeException("Firmware version not found"));

        agent.setNewFirmwareVersion(firmwareVersion);
        return agentRepository.save(agent);

    }

    /**
     * Updates the `updateNeeded` field for a specific agent.
     *
     * @param agentId The ID of the agent to update.
     * @param updateNeeded The new value for the `updateNeeded` field.
     */
    public void updateAgentUpdateNeeded(Long agentId, boolean updateNeeded) {
        Optional<Agent> optionalAgent = agentRepository.findById(agentId);
        if (optionalAgent.isPresent()) {
            Agent agent = optionalAgent.get();
            agent.setUpdateRequested(updateNeeded); // Update the field
            agentRepository.save(agent); // Save changes to the database
        }
    }
    /**
     * Checks if an agent's `updateNeeded` field is set to true.
     *
     * @param agentId The ID of the agent to check.
     * @return true if the agent's `updateNeeded` is true, false otherwise or if the agent is not found.
     */
    public boolean isUpdateAgentUpdateNeeded(Long agentId) {
        return agentRepository.findById(agentId)
                .map(Agent::isUpdateRequested) // Get the value of `updateNeeded`
                .orElse(false); // Return false if the agent is not found
    }
}
