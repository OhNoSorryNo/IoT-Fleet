package IoTFleetManagement.agent.repository;

import IoTFleetManagement.agent.model.Agent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for managing IoT Agents in the fleet management system.
 * <p>
 * This interface provides methods to perform CRUD operations on agents in the database.
 * It extends JpaRepository, which provides common persistence methods, and includes
 * a custom method to find agents by their unique ID.
 */
public interface AgentRepository extends JpaRepository<Agent, Long> {

    /**
     * Finds an agent by its unique agent ID.
     *
     * @param agentId the unique identifier of the agent
     * @return an Optional containing the found agent, or empty if no agent was found
     */
    Optional<Agent> findByAgentId(String agentId);
}
