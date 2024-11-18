package IoTFleetManagement.agent.repository;

import IoTFleetManagement.agent.model.Agent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AgentRepository extends JpaRepository<Agent, Long> {
    Optional<Agent> findByDeviceId(String agentId);
}
