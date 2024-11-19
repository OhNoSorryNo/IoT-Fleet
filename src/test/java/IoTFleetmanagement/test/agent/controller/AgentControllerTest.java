package IoTFleetmanagement.test.agent.controller;

import IoTFleetManagement.agent.controller.AgentController;
import IoTFleetManagement.agent.model.Agent;
import IoTFleetManagement.agent.service.AgentService;
import IoTFleetManagement.agent.controller.StatusUpdateRequest;
import IoTFleetmanagement.test.security.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import java.util.Arrays;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AgentController.class)
@Import(TestSecurityConfig.class)
public class AgentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AgentService agentService;

    @Test
    public void testGetAllAgents() throws Exception {
        // Arrange
        Agent agent1 = new Agent();
        agent1.setAgentId("agent1");
        agent1.setSecretKey("secretKey1");
        agent1.setOnline(true);

        Agent agent2 = new Agent();
        agent2.setAgentId("agent2");
        agent2.setSecretKey("secretKey2");
        agent2.setOnline(false);
        when(agentService.getAllAgents()).thenReturn(Arrays.asList(agent1, agent2));

        // Act & Assert
        mockMvc.perform(get("/agents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].agentId").value("agent1"))
                .andExpect(jsonPath("$[1].agentId").value("agent2"));

        verify(agentService, times(1)).getAllAgents();
    }

    @Test
    public void testAddAgent() throws Exception {
        // Arrange
        Agent agent = new Agent();
        agent.setAgentId("agent1");
        agent.setSecretKey("hashedSecret");
        agent.setOnline(true);
        when(agentService.addAgent(any(Agent.class))).thenReturn(agent);

        // Act & Assert
        mockMvc.perform(post("/agents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"agentId\": \"agent1\", \"secretKey\": \"secretKey\"}")
                        .with(csrf())) // Add CSRF token for the POST request
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.agentId").value("agent1"));

        verify(agentService, times(1)).addAgent(any(Agent.class));
    }

    @Test
    public void testGetAgentStatus() throws Exception {
        // Arrange
        when(agentService.getAgentStatus("agent1")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/agents/agent1/status"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(agentService, times(1)).getAgentStatus("agent1");
    }

    @Test
    public void testUpdateAgentStatus() throws Exception {
        // Arrange
        doNothing().when(agentService).updateAgentStatus("agent1", true);

        // Act & Assert
        mockMvc.perform(put("/agents/agent1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"online\": true}")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Status updated successfully."));

        verify(agentService, times(1)).updateAgentStatus("agent1", true);
    }

    @Test
    public void testGetAgentStatus_NotFound() throws Exception {
        // Arrange
        when(agentService.getAgentStatus("nonexistentAgent")).thenThrow(ChangeSetPersister.NotFoundException.class);

        // Act & Assert
        mockMvc.perform(get("/agents/nonexistentAgent/status"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Resource not found"));;

        verify(agentService, times(1)).getAgentStatus("nonexistentAgent");
    }
}
