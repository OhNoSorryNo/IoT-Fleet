package IoTFleetManagement.agent.controller;

import IoTFleetManagement.agent.model.Agent;
import IoTFleetManagement.agent.service.AgentService;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/agents")
public class AgentController {
    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @GetMapping
    public List<Agent> getAllAgents() {
        return agentService.getAllAgents();
    }

    @PostMapping
    public ResponseEntity<Agent> addAgent(@RequestBody Agent agent) {
        Agent createdAgent = agentService.addAgent(agent);
        return new ResponseEntity<>(createdAgent, HttpStatus.CREATED);
    }

    //endpoint to retrieve the agent status
    @GetMapping("/{agentId}/status")
    public boolean getAgentStatus(@PathVariable String agentId) throws ChangeSetPersister.NotFoundException {
        return agentService.getAgentStatus(agentId);

    }

    //Endpoint to update the agent's status
    @PutMapping("/{agentId}/status")
    public ResponseEntity<String> updateAgentStatus(@PathVariable String agentId, @RequestBody StatusUpdateRequest statusUpdate) throws ChangeSetPersister.NotFoundException {
        agentService.updateAgentStatus(agentId, statusUpdate.isOnline());
        return ResponseEntity.ok("Status updated successfully.");
    }
}
