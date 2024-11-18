package IoTFleetManagement.agent.controller;

import IoTFleetManagement.agent.model.Agent;
import IoTFleetManagement.agent.service.AgentService;
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

    @GetMapping("/{agentid}/status")
    public Agent getAgentStatus(@PathVariable String agentid) {
        return agentService.getAgentStatus(agentid);

    }

    @PostMapping
    public ResponseEntity<Agent> addAgent(@RequestBody Agent agent) {
        Agent createdAgent = agentService.addAgent(agent);
        return new ResponseEntity<>(createdAgent, HttpStatus.CREATED);
    }

}
