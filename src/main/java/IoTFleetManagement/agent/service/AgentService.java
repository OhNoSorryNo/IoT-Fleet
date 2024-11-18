package IoTFleetManagement.agent.service;

import IoTFleetManagement.agent.model.Agent;
import IoTFleetManagement.agent.repository.AgentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgentService {

    private final AgentRepository agentRepository;

    public AgentService(AgentRepository agentRepository) {
        this.agentRepository = agentRepository;
    }

    public List<Agent> getAllAgents() {
        return agentRepository.findAll();
    }

    public Agent getAgentStatus(String agentId) {
        return agentRepository.findByDeviceId(agentId)
                .orElseThrow(() -> new IllegalArgumentException("Device not found"));
    }
}
