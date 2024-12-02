package IoTFleetManagement.agent.dto;

/**
 * Data Transfer Object (DTO) for registering an IoT agent.
 * <p>
 * This class encapsulates the data required to register an agent, including the agent's unique identifier (`agentId`)
 * and its secret key (`secretKey`). The DTO is used to transfer data between the client and server
 * during the agent registration process.
 */
public class AgentRegistrationRequest {
    private String agentId;
    private String secretKey;

    /**
     * Retrieves the unique identifier of the agent.
     *
     * @return the agent's unique identifier
     */
    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    /**
     * Retrieves the secret key of the agent.
     *
     * @return the agent's secret key
     */
    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}