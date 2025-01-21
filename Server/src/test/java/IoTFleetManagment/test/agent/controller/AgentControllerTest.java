package IoTFleetManagment.test.agent.controller;

import IoTFleetManagement.agent.controller.AgentController;
import IoTFleetManagement.agent.model.Agent;
import IoTFleetManagement.agent.repository.AgentCategoryRepository;
import IoTFleetManagement.agent.repository.AgentRepository;
import IoTFleetManagement.agent.service.AgentService;
import IoTFleetManagement.firmware.service.FirmwareVersionService;
import IoTFleetManagement.user.model.User;
import IoTFleetManagement.user.repository.UserRepository;
import IoTFleetManagment.test.security.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import java.util.Arrays;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//@WebMvcTest(controllers = AgentController.class)
@WebMvcTest(AgentController.class)
@Import(TestSecurityConfig.class)
@ContextConfiguration(classes = {AgentController.class})
public class AgentControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @MockBean
    private AgentService agentService;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private FirmwareVersionService firmwareVersionService;
    @MockBean
    private AgentRepository agentRepository;
    @MockBean
    private AgentCategoryRepository categoryRepository;


    @Test
    public void testGetAllAgents() throws Exception {
        // Arrange
        Agent agent1 = new Agent();
        agent1.setAgentId("agent1");
        agent1.setSecretKey("secretKey1");
        agent1.setOnline(false);

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
    public void testRegisterAgent() throws Exception {
        // Arrange
        Agent agent = new Agent();
        agent.setAgentId("agent1");
        agent.setToken("sampleToken");
        when(agentService.addAgent(any(Agent.class))).thenReturn(agent);

        // Act & Assert
        mockMvc.perform(post("/agents/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"agentId\": \"agent1\", \"secretKey\": \"secretKey1\"}")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agentId").value("agent1"))
                .andExpect(jsonPath("$.token").value("sampleToken"));

        verify(agentService, times(1)).addAgent(any(Agent.class));
    }

    @Test
    public void testGetAgentStatus() throws Exception {
        // Arrange
        when(agentService.getAgentStatus("agent1")).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/agents/agent1/status"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(agentService, times(1)).getAgentStatus("agent1");
    }

    @Test
    public void testUpdateAgentStatus() throws Exception {
        // Arrange
        String agentId = "agent1";
        String token = "valid-token";

        // The agentService must exist and the token be valid:
        when(agentService.agentExists(agentId)).thenReturn(true);
        when(agentService.validateToken(token, agentId)).thenReturn(true);

        // Since the controller always calls updateAgentStatus(agentId, true),
        // you must expect 'true' here, not 'false'.
        doNothing().when(agentService).updateAgentStatus(agentId, true);

        // Act & Assert
        mockMvc.perform(
                        put("/agents/status/" + agentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"online\": false}") // The controller ignores this value
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk())
                .andExpect(content().string("Status updated successfully."));

        // Make sure the call was with 'true'
        verify(agentService).updateAgentStatus(agentId, true);
    }

    @Test
    public void testRegisterAgentForUser() throws Exception {
        // Arrange
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testUser");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(mockUser);
        SecurityContextHolder.setContext(securityContext);

        Agent agent = new Agent();
        agent.setAgentId("agent1");
        when(agentService.registerAgentToUser(anyString(), any(User.class))).thenReturn(agent);

        // Act & Assert
        mockMvc.perform(post("/agents/registeragentforuser")
                        .param("secretKey", "secretKey")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agentId").value("agent1"));

        verify(agentService, times(1)).registerAgentToUser(eq("secretKey"), eq(mockUser));
    }


    @Test
    public void testAssignAgentToUser() throws Exception {
        // Arrange
        Agent agent = new Agent();
        agent.setId(1L);
        agent.setAgentId("agent1");
        when(agentService.assignAgentToUser(1L, 2L)).thenReturn(agent);

        // Act & Assert
        mockMvc.perform(post("/agents/1/assign/2")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agentId").value("agent1"));

        verify(agentService, times(1)).assignAgentToUser(1L, 2L);
    }

    @Test
    public void testCheckAgentExists() throws Exception {
        // Arrange
        when(agentService.agentExists("agent1")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/agents/agent1/exists"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(agentService, times(1)).agentExists("agent1");
    }

    @Test
    public void testCheckForFirmwareUpdateAgentNotFound() throws Exception {
        // Arrange
        String agentId = "nonexistentAgent";

        when(agentService.getAgentByAgentId(agentId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/agents/{agentId}/update-check", agentId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Agent not found"));

        verify(agentService, times(1)).getAgentByAgentId(agentId);
        verify(firmwareVersionService, times(0)).getLatestFirmwareVersion();
    }
}

