package IoTFleetManagement.agent.controller;

import IoTFleetManagement.agent.dto.AgentRegistrationRequest;
import IoTFleetManagement.agent.model.Agent;
import IoTFleetManagement.agent.service.AgentService;
import IoTFleetManagement.common.exceptions.AlreadyExistsException;
import IoTFleetManagement.user.model.User;
import IoTFleetManagement.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import java.security.PublicKey;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller class for managing IoT Agents in the fleet management system.
 * <p>
 * This class provides REST API endpoints to manage IoT agents, including adding new agents,
 * retrieving their status, and updating their online status.
 *
 * @author Lara
 * @author Jasmin1707
 * @author Miriam
 */
@RestController
@RequestMapping("/agents")
public class AgentController {
    private static final Logger log = LoggerFactory.getLogger(AgentController.class);
    @Autowired
    private final AgentService agentService;
    @Autowired
    private final UserRepository userRepository;

    /**
     * Constructor to initialize the AgentController with the provided services.
     *
     * @param agentService   the service used to manage agents
     * @param userRepository the repository used to manage users
     */
    public AgentController(AgentService agentService, UserRepository userRepository) {
        this.agentService = agentService;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves a list of all agents in the system.
     *
     * @return a list of all agents
     */
    @GetMapping
    public List<Agent> getAllAgents() {
        return agentService.getAllAgents();
    }

    /**
     * Registers a new agent in the system and generates a JWT token for the agent.
     *
     * @param request the registration request containing `agentId` and `secretKey`
     * @return a ResponseEntity containing the agent ID and generated JWT token, or an error message if the registration fails
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerAgent(@RequestBody AgentRegistrationRequest request) {
        try {
            // Create an Agent object from the registration request
            Agent agent = new Agent();
            agent.setAgentId(request.getAgentId());
            agent.setSecretKey(request.getSecretKey());

            // Use the addAgent method to register the agent
            Agent registeredAgent = agentService.addAgent(agent);
            // Return the agent ID and token in the response
            return ResponseEntity.ok(Map.of(
                    "agentId", registeredAgent.getAgentId(),
                    "token", registeredAgent.getToken()
            ));
        } catch (AlreadyExistsException ex) {
            log.warn("Agent already exists: {}", request.getAgentId());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Agent with ID " + request.getAgentId() + " already exists."));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * Checks if an agent with the specified ID exists in the system.
     *
     * @param agentId the unique identifier of the agent
     * @return a ResponseEntity containing {@code true} if the agent exists, or {@code false} otherwise
     */
    @GetMapping("/{agentId}/exists")
    public ResponseEntity<Boolean> checkAgentExists(@PathVariable String agentId) {
        boolean exists = agentService.agentExists(agentId);
        return ResponseEntity.ok(exists);
    }

    /**
     * Retrieves the online status of a specific agent by its unique ID.
     *
     * @param agentId the unique identifier of the agent
     * @return {@code true} if the agent is online, {@code false} otherwise
     * @throws ChangeSetPersister.NotFoundException if the agent is not found
     */
    //endpoint to retrieve the agent status
    @GetMapping("/{agentId}/status")
    public boolean getAgentStatus(@PathVariable String agentId) throws ChangeSetPersister.NotFoundException {
        return agentService.getAgentStatus(agentId);

    }

    /**
     * Updates the online status of a specific agent.
     *
     * @param agentId           the unique identifier of the agent
     * @param statusUpdate      the status update request containing the new online status
     * @param authorizationHeader the authorization header containing the JWT token
     * @return a ResponseEntity containing a success message or an error message if the operation fails
     * @throws ChangeSetPersister.NotFoundException if the agent with the specified ID is not found
     */
    //Endpoint to update the agent's status
    @PutMapping("/status/{agentId}")
    public ResponseEntity<String> updateAgentStatus(
            @PathVariable String agentId,
            @RequestBody StatusUpdateRequest statusUpdate,
            @RequestHeader("Authorization") String authorizationHeader) {
        log.error("Update status for agent received: {}", agentId);
        try {
            // Extract the token from the Authorization header
            String token = authorizationHeader.replace("Bearer ", "");
            log.info("Token: {}", token);
            // Check if the agent exists in the database
            if (!agentService.agentExists(agentId)) {
                log.error("Agent not found: {}", agentId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Agent not found");
            }
            if(!agentService.validateToken(token, agentId)){
                log.error("Invalid token for agent: {}", token);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token" + token);
            }

            // Check if the request is valid (status update must not be null)
            if (statusUpdate == null) {
                log.warn("Invalid request body for agent: {}", agentId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request body");
            }

            // Update the status in the database
            log.info("Updating status for agent: {}", agentId);
            agentService.updateAgentStatus(agentId, true);

            return ResponseEntity.ok("Status updated successfully.");
        } catch (Exception e) {
            log.error("Failed to update status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update status");
        }
    }


    /**
     * Handles exceptions when an agent is not found in the system.
     *
     * @param ex the exception that was thrown
     * @return a ResponseEntity containing an error message and the HTTPS status
     */
    @ExceptionHandler(ChangeSetPersister.NotFoundException.class)
    public ResponseEntity<String> handleNotFoundException(ChangeSetPersister.NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Resource not found");
    }

    @PostMapping("/getToken")
    public ResponseEntity<?> getTokenAgent(@RequestBody AgentRegistrationRequest request) {
        try {
            String agentId = request.getAgentId();
            String secretKey = request.getSecretKey();

            // Authenticate the agent
            Agent agent = agentService.authenticate(agentId, secretKey);
            // Check if the token is expired or null, generate a new one if necessary
            String token = agent.getToken();

            // Return the agent ID and token in the response
            return ResponseEntity.ok(Map.of(
                    "agentId", agent.getAgentId(),
                    "token", token
            ));
        } catch (ChangeSetPersister.NotFoundException ex) {
            log.warn("Agent not found: {}", request.getAgentId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Agent not found."));
        } catch (AuthenticationException ex) {
            log.warn("Authentication failed for agent: {}", request.getAgentId());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials."));
        }
    }


    /**
     * Registers an agent for the authenticated user based on the provided secret key.
     *
     * @param secretKey the secret key of the agent to be registered
     * @return a ResponseEntity containing the registered Agent object
     * @throws RuntimeException if the user is not authenticated
     */
    @PostMapping("/registeragentforuser")
    public ResponseEntity<Agent> registerAgentForUser(@RequestParam String secretKey) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("No authentication found or user not authenticated");
            throw new RuntimeException("User is not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User) {
            User user = (User) principal;
            log.debug("Authenticated user: {}", user.getUsername());

            Agent agent = agentService.registerAgentToUser(secretKey, user);
            return ResponseEntity.ok(agent);
        } else {
            log.warn("Principal is not of type User. Actual type: {}", principal.getClass().getName());
            throw new RuntimeException("User is not authenticated");
        }
    }

    /**
     * Assigns an agent to a user based on their respective IDs.
     *
     * @param agentId the unique identifier of the agent
     * @param userId  the unique identifier of the user
     * @return a ResponseEntity containing the assigned Agent object
     */
    @PostMapping("/{agentId}/assign/{userId}")
    public ResponseEntity<Agent> assignAgentToUser(@PathVariable Long agentId, @PathVariable Long userId) {
        Agent agent = agentService.assignAgentToUser(agentId, userId);
        return ResponseEntity.ok(agent);
    }

    /**
     * Retrieves the details of a specific agent by its unique ID.
     *
     * @param agentId the unique identifier of the agent
     * @return a ResponseEntity containing the agent details if found, or an error message if not found
     */
    @GetMapping("/{agentId}/details")
    public ResponseEntity<?> getAgentDetails(@PathVariable String agentId) {
        Optional<Agent> agentOptional = agentService.getAgentByAgentId(agentId);

        if (agentOptional.isPresent()) {
            return ResponseEntity.ok(agentOptional.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Agent not found");
        }
    }

    /**
     * Removes an agent from its assigned user by setting the user_id to NULL.
     *
     * @param agentId the unique identifier of the agent to be removed
     * @return a ResponseEntity indicating success or failure of the operation
     */
    @PostMapping("/{agentId}/remove")
    public ResponseEntity<String> removeAgentFromUser(@PathVariable String agentId) {
        try {
            // Retrieve the agent by its agentId
            Optional<Agent> agentOptional = agentService.getAgentByAgentId(agentId);

            if (agentOptional.isEmpty()) {
                log.warn("Agent not found with ID: {}", agentId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Agent not found");
            }

            Agent agent = agentOptional.get();

            // Check if the agent is assigned to a user
            if (agent.getUser() == null) {
                log.info("Agent with ID: {} is already unassigned", agentId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Agent is already unassigned");
            }

            agent.setUser(null);
            agentService.saveAgent(agent);

            log.info("Agent with ID: {} successfully removed from user", agentId);
            return ResponseEntity.ok("Agent successfully removed from user");
        } catch (Exception e) {
            log.error("Error removing agent with ID: {}", agentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to remove agent");
        }
    }

    /**
     * Assigns a firmware version to a specific agent.
     *
     * <p>This endpoint allows associating a firmware identified by its ID
     * with an agent identified by its ID. It updates the agent's record
     * to reflect the assigned firmware version.</p>
     *
     * <p><b>Endpoint:</b> {@code PUT /{agentId}/firmware/{firmwareId}}</p>
     *
     * @param agentId    the unique identifier of the agent to which the firmware will be assigned
     * @param firmwareId the unique identifier of the firmware to be assigned to the agent
     * @return the updated {@link Agent} object reflecting the assigned firmware
     */
    @PutMapping("/{agentId}/firmware/{firmwareId}")
    public Agent assignFirmwareToAgent(@PathVariable Long agentId, @PathVariable Long firmwareId) {
        return agentService.assignFirmwareToAgent(agentId, firmwareId);
    }

}
