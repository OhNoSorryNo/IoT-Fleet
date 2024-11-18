package IoTFleetManagement.agent.service;

import IoTFleetManagement.agent.model.Agent;
import IoTFleetManagement.agent.repository.AgentRepository;
import IoTFleetManagement.common.exceptions.AlreadyExistsException;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgentService {

    private final AgentRepository agentRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AgentService(AgentRepository agentRepository) {
        this.agentRepository = agentRepository;
        this.passwordEncoder = new BCryptPasswordEncoder(); // Initialize once
    }

    public List<Agent> getAllAgents() {
        return agentRepository.findAll();
    }


    public boolean getAgentStatus(String agentId) throws ChangeSetPersister.NotFoundException {
        // Use map to transform the Optional<Agent> into Optional<Boolean> and throw NotFoundException if absent
        return agentRepository.findByAgentId(agentId)
                .map(Agent::isOnline)
                .orElseThrow(ChangeSetPersister.NotFoundException::new);
    }

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

        return agentRepository.save(agent);
    }
}
