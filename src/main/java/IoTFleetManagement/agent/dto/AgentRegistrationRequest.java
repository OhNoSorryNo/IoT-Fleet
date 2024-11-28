package IoTFleetManagement.agent.dto;

public class AgentRegistrationRequest {
    private String agentId;
    private String secretKey;

    // Getters and setters
    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}