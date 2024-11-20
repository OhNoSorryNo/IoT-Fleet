package IoTFleetManagement.agent.controller;

import IoTFleetManagement.agent.model.Agent;
import IoTFleetManagement.agent.service.AgentService;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller class for managing IoT Agents in the fleet management system.
 * <p>
 * This class provides REST API endpoints to manage IoT agents, including adding new agents,
 * retrieving their status, and updating their online status.
 */
@RestController
@RequestMapping("/agents")
public class AgentController {
    private final AgentService agentService;

    /**
     * Constructor to initialize the AgentController with the provided AgentService.
     *
     * @param agentService the service used to manage agents
     */
    public AgentController(AgentService agentService) {
        this.agentService = agentService;
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
     * Adds a new agent to the system.
     *
     * @param agent the agent to be added
     * @return a ResponseEntity containing the created agent and the HTTPS status
     */
    @PostMapping
    public ResponseEntity<Agent> addAgent(@RequestBody Agent agent) {
        Agent createdAgent = agentService.addAgent(agent);
        return new ResponseEntity<>(createdAgent, HttpStatus.CREATED);
    }

    /**
     * Retrieves the online status of a specific agent.
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
     * @param agentId the unique identifier of the agent
     * @param statusUpdate the status update request containing the new online status
     * @return a ResponseEntity with a success message
     * @throws ChangeSetPersister.NotFoundException if the agent is not found
     */
    //Endpoint to update the agent's status
    @PutMapping("/{agentId}/status")
    public ResponseEntity<String> updateAgentStatus(@PathVariable String agentId, @RequestBody StatusUpdateRequest statusUpdate) throws ChangeSetPersister.NotFoundException {
        agentService.updateAgentStatus(agentId, statusUpdate.isOnline());
        return ResponseEntity.ok("Status updated successfully.");
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
}
