package IoTFleetmanagement.test.agent.controller;

import IoTFleetManagement.agent.controller.AgentController;
import IoTFleetManagement.agent.model.Agent;
import IoTFleetManagement.agent.service.AgentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AgentController.class)
public class AgentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AgentService agentService;

    @Test
    public void testGetAllAgents() throws Exception {
        // Mock data
        Agent agent1 = new Agent();
        agent1.setId(1L);
        agent1.setAgentId("agent-123");
        agent1.setOnline(true);
        agent1.setLastSeen(LocalDateTime.now());
        agent1.setToken("token123");
        agent1.setFirmwareVersion("v1.0");
        agent1.setPingFrequency(5);
        agent1.setAgentType("typeA");

        Agent agent2 = new Agent();
        agent2.setId(2L);
        agent2.setAgentId("agent-456");
        agent2.setOnline(false);
        agent2.setLastSeen(LocalDateTime.now().minusMinutes(10));
        agent2.setToken("token456");
        agent2.setFirmwareVersion("v2.0");
        agent2.setPingFrequency(10);
        agent2.setAgentType("typeB");

        List<Agent> agents = Arrays.asList(agent1, agent2);

        // Configure the mock service
        given(agentService.getAllAgents()).willReturn(agents);

        // Perform GET request and verify the response
        mockMvc.perform(get("/agents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].agentId", is("agent-123")))
                .andExpect(jsonPath("$[0].online", is(true)))
                .andExpect(jsonPath("$[1].agentId", is("agent-456")))
                .andExpect(jsonPath("$[1].online", is(false)));
    }

    @Test
    public void testGetAgentStatus() throws Exception {
        // Mock data
        Agent agent = new Agent();
        agent.setId(1L);
        agent.setAgentId("agent-123");
        agent.setOnline(true);
        agent.setLastSeen(LocalDateTime.now());
        agent.setToken("token123");
        agent.setFirmwareVersion("v1.0");
        agent.setPingFrequency(5);
        agent.setAgentType("typeA");

        // Configure the mock service
        given(agentService.getAgentStatus("agent-123")).willReturn(agent.isOnline());

        // Perform GET request and verify the response
        mockMvc.perform(get("/agents/agent-123/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agentId", is("agent-123")))
                .andExpect(jsonPath("$.online", is(true)))
                .andExpect(jsonPath("$.firmwareVersion", is("v1.0")));
    }
}