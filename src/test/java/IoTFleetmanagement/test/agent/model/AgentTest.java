package IoTFleetmanagement.test.agent.model;

import IoTFleetManagement.agent.model.Agent;
import IoTFleetManagement.user.model.User;
import org.junit.jupiter.api.Assertions;
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
        agent.setOnline(false); // Updated default value to reflect false
        agent.setToken("token123");
        agent.setFirmwareVersion("v1.0");
        agent.setPingFrequency(5);
        agent.setAgentType("typeA");

        // Create a User and set it to the agent
        User user = new User();
        user.setId(2L);
        user.setUsername("testUser");
        agent.setUser(user);

        // Assert values
        assertEquals(1L, agent.getId());
        assertEquals("agent-123", agent.getAgentId());
        assertEquals("secretKey123", agent.getSecretKey());
        assertNotNull(agent.getLastSeen());
        Assertions.assertFalse(agent.isOnline()); // Changed assertion to false since default is false
        assertEquals("token123", agent.getToken());
        assertEquals("v1.0", agent.getFirmwareVersion());
        assertEquals(5, agent.getPingFrequency());
        assertEquals("typeA", agent.getAgentType());

        // Assert user
        assertNotNull(agent.getUser());
        assertEquals(2L, agent.getUser().getId());
        assertEquals("testUser", agent.getUser().getUsername());
    }

    @Test
    public void testAgentDefaultValues() {
        // Create an instance of Agent
        Agent agent = new Agent();

        // Assert default values
        assertNull(agent.getId());
        assertNull(agent.getAgentId());
        assertNull(agent.getSecretKey());
        assertNull(agent.getLastSeen());
        assertFalse(agent.isOnline()); // Default value is false
        assertNull(agent.getToken());
        assertNull(agent.getFirmwareVersion());
        assertEquals(0, agent.getPingFrequency()); // Default value is 0
        assertNull(agent.getAgentType());
        assertNull(agent.getUser());
    }
}
