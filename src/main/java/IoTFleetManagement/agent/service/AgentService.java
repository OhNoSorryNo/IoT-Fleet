package IoTFleetManagement.agent.service;

import IoTFleetManagement.agent.model.Agent;
import IoTFleetManagement.agent.repository.AgentRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

    public Agent addAgent(Agent agent) {
        //Validations
        if (agentRepository.findByDeviceId(agent.getAgentId()).isPresent()) {
            throw new IllegalArgumentException("Agent with this deviceId already exists");
        }
        if (agent.getAgentId() == null || agent.getAgentId().isBlank()) {
            throw new IllegalArgumentException("deviceId cannot be null or blank");
        }

        // Hash the secret key before saving
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        agent.setSecretKey(passwordEncoder.encode(agent.getSecretKey()));

        return agentRepository.save(agent);
    }
}
