package IoTFleetmanagement.test.agent.controller;

import IoTFleetManagement.agent.model.Agent;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class AgentTest {

    @Test
    public void testAgentGettersAndSetters() {
        // Create an instance of Agent
        Agent agent = new Agent();

        // Set values
        agent.setId(1L);
        agent.setAgentId("agent-123");
        agent.setSecretKey("secretKey123");
        agent.setLastSeen(LocalDateTime.now());
        agent.setOnline(true);
        agent.setToken("token123");
        agent.setFirmwareVersion("v1.0");
        agent.setPingFrequency(5);
        agent.setAgentType("typeA");

        // Assert values
        assertEquals(1L, agent.getId());
        assertEquals("agent-123", agent.getAgentId());
        assertEquals("secretKey123", agent.getSecretKey());
        assertNotNull(agent.getLastSeen());
        assertTrue(agent.isOnline());
        assertEquals("token123", agent.getToken());
        assertEquals("v1.0", agent.getFirmwareVersion());
        assertEquals(5, agent.getPingFrequency());
        assertEquals("typeA", agent.getAgentType());
    }
}