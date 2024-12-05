package IoTFleetManagement.agent.service;

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

/**
 * Service class for managing IoT Agents in the fleet management system.
 * <p>
 * This class provides the business logic for managing agents, including adding new agents,
 * retrieving agent information, checking agent status, updating status, and assigning agents to users.
 * </p>
 *
 * @author Lara
 * @author Jasmin1707
 */
@Service
public class AgentService {

    @Autowired
    private final AgentRepository agentRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    @Autowired
    private final UserRepository userRepository;
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
        return agentRepository.findAll();
    }


    private static final Logger log = LoggerFactory.getLogger(AgentService.class);

    /**
     * Retrieves the online status of a specific agent.
     *
     * @param agentId the unique identifier of the agent
     * @return {@code true} if the agent is online, {@code false} otherwise
     * @throws ChangeSetPersister.NotFoundException if the agent is not found
     */
    public boolean getAgentStatus(String agentId) throws ChangeSetPersister.NotFoundException {
        // Use map to transform the Optional<Agent> into Optional<Boolean> and throw NotFoundException if absent
        return agentRepository.findByAgentId(agentId)
                .map(Agent::isOnline)
                .orElseThrow(ChangeSetPersister.NotFoundException::new);
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
        // Validation: Check if the agentId is null or blank
        if (agent.getAgentId() == null || agent.getAgentId().isBlank()) {
            throw new IllegalArgumentException("agentId cannot be null or blank");
        }

        // Validation: Check if an agent with the same agentId already exists
        if (agentRepository.findByAgentId(agent.getAgentId()).isPresent()) {
            throw new AlreadyExistsException("Agent with this agentId already exists");
        }

        // Hash the secret key before saving
        agent.setSecretKey(passwordEncoder.encode(agent.getSecretKey()));

        // Generate JWT token
        String token = JwtUtil.generateToken(agent.getAgentId());
        agent.setToken(token);

        return agentRepository.save(agent);
    }

    /**
     * Checks whether an agent exists by its unique agent ID.
     *
     * @param agentId the unique identifier of the agent
     * @return {@code true} if the agent exists, {@code false} otherwise
     */
    public boolean agentExists(String agentId) {
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
        // Retrieve the agent from the database
        Agent agent = agentRepository.findByAgentId(agentId)
                .orElseThrow(ChangeSetPersister.NotFoundException::new);

        // Update the online status
        agent.setOnline(isOnline);

        // Set the lastSeen timestamp to the current time
        agent.setLastSeen(LocalDateTime.now());

        // Save the changes to the database
        agentRepository.save(agent);

        // Log the update for debugging and monitoring purposes
        log.info("Updated status for agent {}: online = {}, lastSeen = {}", agentId, isOnline, agent.getLastSeen());
    }

    /**
     * Validates a JWT token against an agent ID.
     *
     * @param token   the JWT token to validate
     * @param agentId the agent ID the token is associated with
     * @return {@code true} if the token is valid, {@code false} otherwise
     */
    public boolean validateToken(String token, String agentId) {
        return jwtUtil.validateToken(token, agentId);
    }

    // Check the status of all devices every 30 seconds
    @Scheduled(fixedRate = 10000) // Every 30 seconds
    public void checkAllDevicesStatus() {
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

    public Agent authenticate(String agentId, String secretKey) throws ChangeSetPersister.NotFoundException, AuthenticationException {
        Agent agent = agentRepository.findByAgentId(agentId)
                .orElseThrow(ChangeSetPersister.NotFoundException::new);

        if (passwordEncoder.matches(secretKey, agent.getSecretKey())) {
            return agent;
        } else {
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
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        agent.setUser(user);
        return agentRepository.save(agent);
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
        // Getting all the agents
        List<Agent> agents = agentRepository.findAll();
        // Find the agent by its secret key
        Agent agent = agents.stream()
                .filter(a -> passwordEncoder.matches(secretKey, a.getSecretKey()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid secret key"));


        // Check if the agent is already assigned to another user
        if (agent.getUser() != null) {
            throw new RuntimeException("This agent is already registered to another user");
        }

        // Assign the agent to the user
        agent.setUser(user);
        Agent savedAgent = agentRepository.save(agent);
        log.info("Successfully assigned user: {} to agent: {}", user, savedAgent);
        //return agentRepository.save(savedAgent);
        return savedAgent;
    }

    public BCryptPasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }
}
