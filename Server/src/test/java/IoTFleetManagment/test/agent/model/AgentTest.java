package IoTFleetManagment.test.agent.model;

import IoTFleetManagement.agent.model.Agent;
import IoTFleetManagement.agent.model.AgentCategory;
import IoTFleetManagement.firmware.model.FirmwareVersion;
import IoTFleetManagement.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AgentTest {

    private Agent agent;

    @BeforeEach
    void setUp() {
        agent = new Agent();
    }

    @Test
    void testSetAndGetId() {
        Long id = 1L;
        agent.setId(id);
        assertEquals(id, agent.getId());
    }

    @Test
    void testSetAndGetAgentId() {
        String agentId = "agent123";
        agent.setAgentId(agentId);
        assertEquals(agentId, agent.getAgentId());
    }

    @Test
    void testSetAndGetSecretKey() {
        String secretKey = "hashedSecret";
        agent.setSecretKey(secretKey);
        assertEquals(secretKey, agent.getSecretKey());
    }

    @Test
    void testSetAndGetCategories() {
        Set<AgentCategory> categories = new HashSet<>();
        AgentCategory category = new AgentCategory();
        categories.add(category);

        agent.setCategories(categories);
        assertEquals(categories, agent.getCategories());
    }

    @Test
    void testSetAndGetLastSeen() {
        LocalDateTime now = LocalDateTime.now();
        agent.setLastSeen(now);
        assertEquals(now, agent.getLastSeen());
    }

    @Test
    void testSetAndIsOnline() {
        agent.setOnline(true);
        assertTrue(agent.isOnline());
    }

    @Test
    void testSetAndGetToken() {
        String token = "jwtToken";
        agent.setToken(token);
        assertEquals(token, agent.getToken());
    }

    @Test
    void testSetAndGetFirmwareVersion() {
        FirmwareVersion firmwareVersion = new FirmwareVersion();
        agent.setFirmwareVersion(firmwareVersion);
        assertEquals(firmwareVersion, agent.getFirmwareVersion());
    }

    @Test
    void testSetAndGetNewFirmware() {
        FirmwareVersion newFirmware = new FirmwareVersion();
        agent.setNewFirmware(newFirmware);
        assertEquals(newFirmware, agent.getNewFirmware());
    }

    @Test
    void testSetAndGetPingFrequency() {
        int frequency = 5000;
        agent.setPingFrequency(frequency);
        assertEquals(frequency, agent.getPingFrequency());
    }

    @Test
    void testSetAndGetAgentType() {
        String type = "sensor";
        agent.setAgentType(type);
        assertEquals(type, agent.getAgentType());
    }

    @Test
    void testSetAndGetUser() {
        User user = new User();
        user.setUsername("testUser");
        agent.setUser(user);
        assertEquals(user, agent.getUser());
    }

    @Test
    void testSetAndIsUpdateRequested() {
        agent.setUpdateRequested(true);
        assertTrue(agent.isUpdateRequested());
    }
}
