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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for managing IoT Agents in the fleet management system.
 * <p>
 * This class provides business logic for managing agents, including adding new agents,
 * retrieving all agents, getting the status of specific agents, and updating their status.
 */
@Service
public class AgentService {

    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    private final AgentRepository agentRepository;
    @Autowired
    private final UserRepository userRepository;

    /**
     * Constructor to initialize the AgentService with the provided AgentRepository.
     *
     * @param agentRepository the repository used to perform CRUD operations on agents
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
     * Retrieves the online status of a specific agent by its unique agent ID.
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
     *
     * <p>The agent's secret key is hashed before saving, and validation is performed to ensure the agent ID is unique.</p>
     *
     * @param agent the agent to be added
     * @return the added agent
     * @throws IllegalArgumentException if the agent ID is null or blank
     * @throws AlreadyExistsException if an agent with the same agent ID already exists
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
     * Updates the online status of a specific agent by its unique agent ID.
     *
     * @param agentId the unique identifier of the agent
     * @throws ChangeSetPersister.NotFoundException if the agent is not found
     */

    public boolean agentExists(String agentId) {
        return agentRepository.findByAgentId(agentId).isPresent();
    }

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

    public boolean validateToken(String token, String agentId) {
        return jwtUtil.validateToken(token, agentId);
    }

    public Agent assignAgentToUser(Long agentId, Long userId) {
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        agent.setUser(user);
        return agentRepository.save(agent);
    }

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
        return agentRepository.save(savedAgent);
    }

//    @Transactional
//    public void saveAgent(Agent agent) {
//        agentRepository.save(agent);
//    }



}
