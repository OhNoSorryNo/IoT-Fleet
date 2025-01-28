package IoTFleetManagment.test.agent.controller;

import IoTFleetManagement.agent.controller.AgentController;
import IoTFleetManagement.agent.dto.AgentRegistrationRequest;
import IoTFleetManagement.agent.model.Agent;
import IoTFleetManagement.agent.model.AgentCategory;
import IoTFleetManagement.agent.repository.AgentCategoryRepository;
import IoTFleetManagement.agent.repository.AgentRepository;
import IoTFleetManagement.agent.service.AgentService;
import IoTFleetManagement.common.exceptions.AlreadyExistsException;
import IoTFleetManagement.firmware.model.FirmwareVersion;
import IoTFleetManagement.firmware.service.FirmwareVersionService;
import IoTFleetManagement.user.model.User;
import IoTFleetManagement.user.repository.UserRepository;
import IoTFleetManagment.test.security.config.TestSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import java.util.*;

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
    public void testRegisterAgentAlreadyExists() throws Exception {
        // Arrange
        AgentRegistrationRequest request = new AgentRegistrationRequest();
        request.setAgentId("existingAgent");
        request.setSecretKey("secretKey");

        // Simulate that the agent already exists
        doThrow(new AlreadyExistsException("Agent already exists"))
                .when(agentService)
                .addAgent(any(Agent.class));

        // Act & Assert
        mockMvc.perform(post("/agents/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"agentId\": \"existingAgent\", \"secretKey\": \"secretKey\"}")
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Agent with ID existingAgent already exists."));

        verify(agentService, times(1)).addAgent(any(Agent.class));
    }

    @Test
    public void testRegisterAgentIllegalArgumentException() throws Exception {
        // Arrange
        AgentRegistrationRequest request = new AgentRegistrationRequest();
        request.setAgentId("invalidAgent");
        request.setSecretKey("secretKey");

        // Simulate an IllegalArgumentException
        doThrow(new IllegalArgumentException("Invalid agent ID"))
                .when(agentService)
                .addAgent(any(Agent.class));

        // Act & Assert
        mockMvc.perform(post("/agents/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"agentId\": \"invalidAgent\", \"secretKey\": \"secretKey\"}")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid agent ID"));

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
    public void testUpdateAgentStatusAgentNotFound() throws Exception {
        // Arrange
        String agentId = "nonexistentAgent";
        String token = "valid-token";
        when(agentService.agentExists(agentId)).thenReturn(false); // Simulate agent does not exist

        // Act & Assert
        mockMvc.perform(put("/agents/status/" + agentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"online\": true}")
                        .header("Authorization", "Bearer " + token)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Agent not found"));

        verify(agentService, times(1)).agentExists(agentId);
        verify(agentService, never()).validateToken(anyString(), anyString());
        verify(agentService, never()).updateAgentStatus(anyString(), anyBoolean());
    }

    @Test
    public void testUpdateAgentStatusInvalidToken() throws Exception {
        // Arrange
        String agentId = "agent1";
        String token = "invalid-token";
        when(agentService.agentExists(agentId)).thenReturn(true); // Simulate agent exists
        when(agentService.validateToken(token, agentId)).thenReturn(false); // Simulate token is invalid

        // Act & Assert
        mockMvc.perform(put("/agents/status/" + agentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"online\": true}")
                        .header("Authorization", "Bearer " + token)
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid token" + token));

        verify(agentService, times(1)).agentExists(agentId);
        verify(agentService, times(1)).validateToken(token, agentId);
        verify(agentService, never()).updateAgentStatus(anyString(), anyBoolean());
    }

    @Test
    public void testUpdateAgentStatusInternalServerError() throws Exception {
        // Arrange
        String agentId = "agent1";
        String token = "valid-token";
        when(agentService.agentExists(agentId)).thenReturn(true); // Simulate agent exists
        when(agentService.validateToken(token, agentId)).thenReturn(true); // Simulate valid token
        doThrow(new RuntimeException("Database error")).when(agentService).updateAgentStatus(agentId, true); // Simulate exception

        // Act & Assert
        mockMvc.perform(put("/agents/status/" + agentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"online\": true}")
                        .header("Authorization", "Bearer " + token)
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to update status"));

        verify(agentService, times(1)).agentExists(agentId);
        verify(agentService, times(1)).validateToken(token, agentId);
        verify(agentService, times(1)).updateAgentStatus(agentId, true);
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
    void testRegisterAgentForUser_NoAuthentication_DirectCall() {
        // Arrange
        AgentController controller =
                new AgentController(agentService, categoryRepository);

        // Clear the security context
        SecurityContextHolder.clearContext();

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> controller.registerAgentForUser("someSecretKey"));
        assertEquals("User is not authenticated", ex.getMessage());

        verify(agentService, never()).registerAgentToUser(anyString(), any());
    }

    @Test
    void testRegisterAgentForUser_NotAuthenticated_DirectCall() {
        // Create a real or mocked controller
        AgentController controller =
                new AgentController(agentService,  categoryRepository);

        // Clear the security context so it’s definitely null
        SecurityContextHolder.clearContext();

        // Act & Assert
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> controller.registerAgentForUser("someSecretKey")
        );
        assertEquals("User is not authenticated", ex.getMessage());

        // Make sure we never invoked the service
        verify(agentService, never()).registerAgentToUser(anyString(), any());
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

    @Test
    void testCheckForFirmwareUpdate_NewFirmwareIsNull() throws Exception {
        // Arrange
        String agentId = "agent123";

        // Mock an Agent that has no newFirmware
        Agent agent = new Agent();
        agent.setAgentId(agentId);

        // Instead of using a String, create a FirmwareVersion object:
        FirmwareVersion existingFirmware = new FirmwareVersion();
        existingFirmware.setTag("v1");
        agent.setFirmwareVersion(existingFirmware);

        // 'newFirmware' is not set -> null
        agent.setNewFirmware(null);

        when(agentService.getAgentByAgentId(agentId))
                .thenReturn(Optional.of(agent));

        // Act & Assert
        mockMvc.perform(get("/agents/{agentId}/update-check", agentId))
                .andExpect(status().isUnprocessableEntity()) // 422
                .andExpect(jsonPath("$.error").value("Unprocessable Entity"))
                .andExpect(jsonPath("$.message")
                        .value("The process cannot be executed because 'newFirmware' is not set."));

        // Verify the service call
        verify(agentService).getAgentByAgentId(agentId);
    }

    @Test
    void testCheckForFirmwareUpdate_SameTagNoUpdate() throws Exception {
        String agentId = "agent123";

        FirmwareVersion currentFw = new FirmwareVersion();
        currentFw.setTag("v1");

        Agent agent = new Agent();
        agent.setAgentId(agentId);
        agent.setFirmwareVersion(currentFw);

        FirmwareVersion newFw = new FirmwareVersion();
        newFw.setTag("v1");
        newFw.setUrl("registry.example.com/image");
        newFw.setImageName("some-image");
        agent.setNewFirmware(newFw);

        when(agentService.getAgentByAgentId(agentId)).thenReturn(Optional.of(agent));
        // Force it to return false => final status becomes false
        when(agentService.isUpdateAgentUpdateNeeded(agentId)).thenReturn(false);

        mockMvc.perform(get("/agents/{agentId}/update-check", agentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(false))  // now it will be false
                .andExpect(jsonPath("$.registry_url").value("registry.example.com/image"))
                .andExpect(jsonPath("$.image_name").value("some-image"))
                .andExpect(jsonPath("$.tag").value("v1"));
    }

    @Test
    void testCheckForFirmwareUpdate_DifferentTagButNoUpdate() throws Exception {
        String agentId = "agent123";

        // Agent's current firmware version: v1
        FirmwareVersion currentFw = new FirmwareVersion();
        currentFw.setTag("v1");

        Agent agent = new Agent();
        agent.setAgentId(agentId);
        agent.setFirmwareVersion(currentFw);

        // The "latestFirmware" object has a different tag (v2)
        FirmwareVersion firmware = new FirmwareVersion();
        firmware.setTag("v2");
        firmware.setUrl("registry.example.com/image2");
        firmware.setImageName("some-other-image");
        agent.setNewFirmware(firmware);

        when(agentService.getAgentByAgentId(agentId)).thenReturn(Optional.of(agent));

        // We say "no update" needed
        when(agentService.isUpdateAgentUpdateNeeded(agentId)).thenReturn(false);

        mockMvc.perform(get("/agents/{agentId}/update-check", agentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(false))  // Because isUpdateAgentUpdateNeeded(...) is false
                .andExpect(jsonPath("$.registry_url").value("registry.example.com/image2"))
                .andExpect(jsonPath("$.tag").value("v2"))
                .andExpect(jsonPath("$.image_name").value("some-other-image"));

        verify(agentService).getAgentByAgentId(agentId);
        verify(agentService).isUpdateAgentUpdateNeeded(agentId);
    }

    @Test
    void testCheckForFirmwareUpdate_DifferentTagUpdateNeeded() throws Exception {
        String agentId = "agent123";

        // Current firmware "v1"
        FirmwareVersion currentFw = new FirmwareVersion();
        currentFw.setTag("v1");

        Agent agent = new Agent();
        agent.setAgentId(agentId);
        agent.setFirmwareVersion(currentFw);

        // New firmware "v2"
        FirmwareVersion firmware = new FirmwareVersion();
        firmware.setTag("v2");
        firmware.setUrl("registry.example.com/image2");
        firmware.setImageName("some-other-image");
        agent.setNewFirmware(firmware);

        when(agentService.getAgentByAgentId(agentId)).thenReturn(Optional.of(agent));
        // Now we say update is needed
        when(agentService.isUpdateAgentUpdateNeeded(agentId)).thenReturn(true);

        mockMvc.perform(get("/agents/{agentId}/update-check", agentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true)) // This time it's true
                .andExpect(jsonPath("$.registry_url").value("registry.example.com/image2"))
                .andExpect(jsonPath("$.tag").value("v2"))
                .andExpect(jsonPath("$.image_name").value("some-other-image"));

        verify(agentService).getAgentByAgentId(agentId);
        verify(agentService).isUpdateAgentUpdateNeeded(agentId);
    }

    @Test
    void testCheckForFirmwareUpdate_FirmwareVersionNull_UpdateNeeded() throws Exception {
        String agentId = "agent123";

        Agent agent = new Agent();
        agent.setAgentId(agentId);
        // The agent's current firmware is null
        agent.setFirmwareVersion(null);

        // The agent's newFirmware has tag "v2"
        FirmwareVersion firmware = new FirmwareVersion();
        firmware.setTag("v2");
        firmware.setUrl("registry.example.com/image2");
        firmware.setImageName("some-other-image");
        agent.setNewFirmware(firmware);

        when(agentService.getAgentByAgentId(agentId)).thenReturn(Optional.of(agent));
        when(agentService.isUpdateAgentUpdateNeeded(agentId)).thenReturn(true);

        mockMvc.perform(get("/agents/{agentId}/update-check", agentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.registry_url").value("registry.example.com/image2"))
                .andExpect(jsonPath("$.tag").value("v2"))
                .andExpect(jsonPath("$.image_name").value("some-other-image"));

        verify(agentService).getAgentByAgentId(agentId);
        verify(agentService).isUpdateAgentUpdateNeeded(agentId);
    }

    @Test
    void testCheckForFirmwareUpdate_ServiceException() throws Exception {
        String agentId = "agent123";

        // Simulate an exception from the service layer
        when(agentService.getAgentByAgentId(agentId))
                .thenThrow(new RuntimeException("DB Error"));

        mockMvc.perform(get("/agents/{agentId}/update-check", agentId)
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("DB Error"));

        verify(agentService).getAgentByAgentId(agentId);
    }


    @Test
    public void testHandleNotFoundExceptionWithMockMvc() throws Exception {
        // Arrange
        String agentId = "nonexistentAgent";

        // Wrap the checked exception in an unchecked exception
        when(agentService.getAgentByAgentId(agentId))
                .thenThrow(new RuntimeException(new ChangeSetPersister.NotFoundException()));

        // Act & Assert
        mockMvc.perform(get("/agents/{agentId}/details", agentId)
                        .with(csrf()))
                .andExpect(status().isNotFound()) // Expect 404 NOT FOUND
                .andExpect(content().string("Resource not found")); // Validate the response body

        // Verify that the service was called
        verify(agentService, times(1)).getAgentByAgentId(agentId);
    }

    @Test
    void testGetTokenAgentSuccess() throws Exception {
        // Arrange
        String agentId = "agent123";
        String secretKey = "mySecretKey";

        // Mock the returned Agent from the service
        Agent mockAgent = new Agent();
        mockAgent.setAgentId(agentId);
        mockAgent.setToken("valid-token");

        // When authenticate is called with these parameters, return mockAgent
        when(agentService.authenticate(agentId, secretKey)).thenReturn(mockAgent);

        // Act & Assert
        mockMvc.perform(post("/agents/getToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"agentId\":\"" + agentId + "\", \"secretKey\":\"" + secretKey + "\"}")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agentId").value(agentId))
                .andExpect(jsonPath("$.token").value("valid-token"));

        // Verify that the service method was called exactly once with the correct parameters
        verify(agentService, times(1)).authenticate(agentId, secretKey);
    }

    @Test
    void testGetTokenAgentNotFound() throws Exception {
        // Arrange
        String agentId = "unknownAgent";
        String secretKey = "someSecret";

        // Instruct the service to throw a NotFoundException
        when(agentService.authenticate(agentId, secretKey))
                .thenThrow(new ChangeSetPersister.NotFoundException());

        // Act & Assert
        mockMvc.perform(post("/agents/getToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"agentId\":\"" + agentId + "\", \"secretKey\":\"" + secretKey + "\"}")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Agent not found."));

        verify(agentService, times(1)).authenticate(agentId, secretKey);
    }

    @Test
    void testGetTokenAgentInvalidCredentials() throws Exception {
        // Arrange
        String agentId = "agentInvalid";
        String secretKey = "wrongSecret";

        // Mock an AuthenticationException
        when(agentService.authenticate(agentId, secretKey))
                .thenThrow(new javax.naming.AuthenticationException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/agents/getToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"agentId\":\"" + agentId + "\", \"secretKey\":\"" + secretKey + "\"}")
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid credentials."));

        verify(agentService, times(1)).authenticate(agentId, secretKey);
    }

    @Test
    void testGetAgentDetails_Found() throws Exception {
        // Arrange
        String agentId = "agent123";
        Agent agent = new Agent();
        agent.setAgentId(agentId);
        agent.setSecretKey("secret");
        // Set any other fields if needed

        // The service returns an Optional containing the agent
        when(agentService.getAgentByAgentId(agentId)).thenReturn(Optional.of(agent));

        // Act & Assert
        mockMvc.perform(get("/agents/{agentId}/details", agentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agentId").value(agentId));

        // Verify the service call
        verify(agentService, times(1)).getAgentByAgentId(agentId);
    }

    @Test
    void testGetAgentDetails_NotFound() throws Exception {
        // Arrange
        String agentId = "unknownAgent";

        // The service returns an empty Optional
        when(agentService.getAgentByAgentId(agentId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/agents/{agentId}/details", agentId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Agent not found"));

        // Verify the service call
        verify(agentService, times(1)).getAgentByAgentId(agentId);
    }

    @Test
    void testRemoveAgentFromUser_AgentNotFound() throws Exception {
        // Arrange
        String agentId = "nonexistentAgent";
        when(agentService.getAgentByAgentId(agentId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/agents/{agentId}/remove", agentId)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Agent not found"));

        // Verify the call
        verify(agentService, times(1)).getAgentByAgentId(agentId);
        verify(agentService, never()).saveAgent(any());
    }

    @Test
    void testRemoveAgentFromUser_AgentAlreadyUnassigned() throws Exception {
        // Arrange
        String agentId = "agent123";
        Agent agent = new Agent();
        agent.setAgentId(agentId);
        agent.setUser(null); // no user assigned

        when(agentService.getAgentByAgentId(agentId)).thenReturn(Optional.of(agent));

        // Act & Assert
        mockMvc.perform(post("/agents/{agentId}/remove", agentId)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Agent is already unassigned"));

        // Verify
        verify(agentService, times(1)).getAgentByAgentId(agentId);
        verify(agentService, never()).saveAgent(any());
    }

    @Test
    void testRemoveAgentFromUser_Success() throws Exception {
        // Arrange
        String agentId = "agent123";
        Agent agent = new Agent();
        agent.setAgentId(agentId);

        User user = new User();
        user.setId(42L);
        // Optionally set other fields, e.g. user.setUsername("testUser")

        agent.setUser(user); // so the agent is assigned

        when(agentService.getAgentByAgentId(agentId)).thenReturn(Optional.of(agent));

        // Act & Assert
        mockMvc.perform(post("/agents/{agentId}/remove", agentId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Agent successfully removed from user"));

        // Verify agent was retrieved
        verify(agentService, times(1)).getAgentByAgentId(agentId);

        // Verify agent was saved with no user
        verify(agentService, times(1)).saveAgent(argThat(savedAgent ->
                savedAgent.getUser() == null &&
                        savedAgent.getAgentId().equals(agentId)
        ));
    }

    @Test
    void testRemoveAgentFromUser_Exception() throws Exception {
        // Arrange
        String agentId = "agent123";
        Agent agent = new Agent();
        agent.setAgentId(agentId);
        User user = new User();
        user.setId(42L);
        agent.setUser(user);

        when(agentService.getAgentByAgentId(agentId)).thenReturn(Optional.of(agent));
        // Simulate an exception when saving
        doThrow(new RuntimeException("DB error")).when(agentService).saveAgent(any(Agent.class));

        // Act & Assert
        mockMvc.perform(post("/agents/{agentId}/remove", agentId)
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to remove agent"));

        verify(agentService, times(1)).getAgentByAgentId(agentId);
        verify(agentService, times(1)).saveAgent(agent);
    }

    @Test
    void testAssignFirmwareToAgent_Success() throws Exception {
        // Arrange
        Long agentId = 123L;
        Long firmwareId = 456L;

        Agent mockAgent = new Agent();
        mockAgent.setId(agentId);
        mockAgent.setAgentId("testAgentId");

        // Mock the service response
        when(agentService.assignFirmwareToAgent(agentId, firmwareId)).thenReturn(mockAgent);

        // Act & Assert
        mockMvc.perform(put("/agents/{agentId}/firmware/{firmwareId}", agentId, firmwareId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))  // If your tests use CSRF, don't forget this
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(123))
                .andExpect(jsonPath("$.agentId").value("testAgentId"));

        verify(agentService, times(1)).assignFirmwareToAgent(agentId, firmwareId);
    }

    @Test
    void testAssignFirmwareToAgent_ServiceThrowsException_DirectCall() {
        // Arrange: a real (or partial) controller
        AgentController controller = new AgentController(agentService, categoryRepository);

        Long agentId = 123L;
        Long firmwareId = 456L;

        when(agentService.assignFirmwareToAgent(agentId, firmwareId))
                .thenThrow(new RuntimeException("DB Error"));

        // Act & Assert: call the method directly
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> controller.assignFirmwareToAgent(agentId, firmwareId)
        );
        assertEquals("DB Error", ex.getMessage());

        // (Optionally) verify the service call:
        verify(agentService, times(1)).assignFirmwareToAgent(agentId, firmwareId);
    }

    @Test
    void testApplyActionToAgents_MissingAction() throws Exception {
        // Arrange: No "action" in the request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("devices", List.of("agent1", "agent2")); // but no "action"

        // Act & Assert
        mockMvc.perform(post("/agents/apply-action")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestBody))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Action or agent list is missing."));

        // We don't expect any service calls
        verify(agentService, never()).getAgentByAgentId(anyString());
    }

    @Test
    void testApplyActionToAgents_EmptyDevices() throws Exception {
        // Arrange: "action" is present, but devices is empty
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("action", "update");
        requestBody.put("devices", Collections.emptyList());

        // Act & Assert
        mockMvc.perform(post("/agents/apply-action")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestBody))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Action or agent list is missing."));

        verify(agentService, never()).getAgentByAgentId(anyString());
    }

    @Test
    void testApplyActionToAgents_AllAgentsFound() throws Exception {
        // Arrange
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("action", "update");
        requestBody.put("devices", List.of("agent1", "agent2"));

        // Mock that both agents exist
        Agent agent1 = new Agent();
        agent1.setAgentId("agent1");
        Agent agent2 = new Agent();
        agent2.setAgentId("agent2");

        when(agentService.getAgentByAgentId("agent1")).thenReturn(Optional.of(agent1));
        when(agentService.getAgentByAgentId("agent2")).thenReturn(Optional.of(agent2));

        // Act & Assert
        mockMvc.perform(post("/agents/apply-action")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestBody))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Action applied successfully to the selected agents."));

        // Verify we looked up both agents
        verify(agentService, times(1)).getAgentByAgentId("agent1");
        verify(agentService, times(1)).getAgentByAgentId("agent2");
    }

    @Test
    void testApplyActionToAgents_SomeAgentsNotFound() throws Exception {
        // Arrange
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("action", "update");
        requestBody.put("devices", List.of("agent1", "missingAgent"));

        // agent1 is found, missingAgent is not
        Agent agent1 = new Agent();
        agent1.setAgentId("agent1");

        when(agentService.getAgentByAgentId("agent1")).thenReturn(Optional.of(agent1));
        when(agentService.getAgentByAgentId("missingAgent")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/agents/apply-action")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestBody))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Action applied successfully to the selected agents."));

        // Verify the calls
        verify(agentService).getAgentByAgentId("agent1");
        verify(agentService).getAgentByAgentId("missingAgent");

        // We can't verify the warn log without a logger spy, but it's happening
    }

    @Test
    void testApplyActionToAgents_ServiceThrowsException() throws Exception {
        // Arrange
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("action", "update");
        requestBody.put("devices", List.of("agent1"));

        when(agentService.getAgentByAgentId("agent1"))
                .thenThrow(new RuntimeException("DB Error")); // or some other exception

        // Act & Assert
        mockMvc.perform(post("/agents/apply-action")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestBody))
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An error occurred while applying the action."));

        verify(agentService).getAgentByAgentId("agent1");
    }

    @Test
    void testApplyActionToAgent_UpdateAction() throws Exception {
        // Arrange
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("action", "update");
        requestBody.put("devices", List.of("agent1"));

        Agent agent = new Agent();
        agent.setAgentId("agent1");

        // The agent is found
        when(agentService.getAgentByAgentId("agent1")).thenReturn(Optional.of(agent));

        // Act & Assert
        mockMvc.perform(post("/agents/apply-action")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestBody))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Action applied successfully to the selected agents."));

        // Verify we looked up the agent
        verify(agentService, times(1)).getAgentByAgentId("agent1");
    }

    @Test
    void testApplyActionToAgent_UnknownAction() throws Exception {
        // Arrange
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("action", "invalidAction");
        requestBody.put("devices", List.of("agent1"));

        Agent agent = new Agent();
        agent.setAgentId("agent1");

        when(agentService.getAgentByAgentId("agent1")).thenReturn(Optional.of(agent));

        // Act & Assert
        mockMvc.perform(post("/agents/apply-action")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestBody))
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An error occurred while applying the action."));

        verify(agentService).getAgentByAgentId("agent1");
    }

    @Test
    void testAssignTagsToAgent_Success() throws Exception {
        // Arrange
        Long agentId = 123L;
        List<Long> categoryIds = List.of(10L, 20L, 30L);

        // We don't need a return value from agentService;
        // just ensure it doesn't throw an exception
        doNothing().when(agentService).assignCategoriesToAgent(agentId, categoryIds);

        // Act & Assert
        mockMvc.perform(post("/agents/{agentId}/tags", agentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(categoryIds))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Tags assigned successfully to agent with ID: " + agentId));

        // Verify that the service method was called
        verify(agentService, times(1)).assignCategoriesToAgent(agentId, categoryIds);
    }

    @Test
    void testAssignTagsToAgent_NotFound() throws Exception {
        // Arrange
        Long agentId = 123L;
        List<Long> categoryIds = List.of(10L, 20L);

        // Simulate that the agent is not found, or some other runtime error
        doThrow(new RuntimeException("Agent not found"))
                .when(agentService).assignCategoriesToAgent(agentId, categoryIds);

        // Act & Assert
        mockMvc.perform(post("/agents/{agentId}/tags", agentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(categoryIds))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Agent not found"));

        // Verify the service method was called
        verify(agentService, times(1)).assignCategoriesToAgent(agentId, categoryIds);
    }

    @Test
    void testFilterAgentsByTag_Found() throws Exception {
        // Arrange
        String tagName = "alpha";
        Agent agent1 = new Agent();
        agent1.setAgentId("agent1");
        Agent agent2 = new Agent();
        agent2.setAgentId("agent2");

        List<Agent> foundAgents = List.of(agent1, agent2);
        when(agentService.findAgentsByCategory(tagName)).thenReturn(foundAgents);

        // Act & Assert
        mockMvc.perform(get("/agents/filter")
                        .param("tagName", tagName)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].agentId").value("agent1"))
                .andExpect(jsonPath("$[1].agentId").value("agent2"));

        // Ensure the service was called with the correct tagName
        verify(agentService).findAgentsByCategory(tagName);
    }

    @Test
    void testFilterAgentsByTag_NotFound() throws Exception {
        // Arrange
        String tagName = "unknown";
        when(agentService.findAgentsByCategory(tagName)).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/agents/filter")
                        .param("tagName", tagName)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.size()").value(0));

        // Ensure the service was called with the correct tagName
        verify(agentService).findAgentsByCategory(tagName);
    }

    @Test
    void testSetUpdateRequestFlag_Success() throws Exception {
        // Arrange
        String agentId = "agent123";

        // Service call doesn't throw an exception
        doNothing().when(agentService).setUpdateRequested(agentId, true);

        // Act & Assert
        mockMvc.perform(put("/agents/{agentId}/update-request", agentId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Update request flag set to true for agent ID: " + agentId));

        // Verify the service call
        verify(agentService, times(1)).setUpdateRequested(agentId, true);
    }

    @Test
    void testSetUpdateRequestFlag_NotFound() throws Exception {
        // Arrange
        String agentId = "agent123";

        // Simulate that the agent is not found or some other runtime error
        doThrow(new RuntimeException("Agent not found"))
                .when(agentService).setUpdateRequested(agentId, true);

        // Act & Assert
        mockMvc.perform(put("/agents/{agentId}/update-request", agentId)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Agent not found"));

        // Verify the service call
        verify(agentService, times(1)).setUpdateRequested(agentId, true);
    }

    @Test
    void testUpdateAgentFirmwareStatus_Success() throws Exception {
        String agentId = "agent123";
        String status = "success";
        String deviceId = "SimulatedDevice123";

        // Create a mock Agent
        Agent agent = new Agent();
        agent.setAgentId(agentId);

        // The service returns Optional.of(agent)
        when(agentService.getAgentByAgentId(agentId)).thenReturn(Optional.of(agent));

        // Prepare request body
        Map<String, Object> body = new HashMap<>();
        body.put("status", status);
        body.put("deviceId", deviceId);

        mockMvc.perform(put("/agents/{agentId}/update-status", agentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(body))
                        .header("Authorization", "Bearer someToken") // optional
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Firmware update success for agentId: " + agentId));

        // Verify that agentService.setUpdateRequested(..., false) was called
        verify(agentService).setUpdateRequested(agentId, false);

        // Verify that agentService.setCurrentFirmwareAfterUpdate(agentId) was called
        verify(agentService).setCurrentFirmwareAfterUpdate(agentId);
    }

    @Test
    void testUpdateAgentFirmwareStatus_Failure() throws Exception {
        String agentId = "agent123";
        String status = "failure";

        // Create a mock Agent
        Agent agent = new Agent();
        agent.setAgentId(agentId);

        when(agentService.getAgentByAgentId(agentId)).thenReturn(Optional.of(agent));

        // Prepare request body
        Map<String, Object> body = new HashMap<>();
        body.put("status", status);
        body.put("deviceId", "AnotherDeviceId");

        mockMvc.perform(put("/agents/{agentId}/update-status", agentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(body))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Firmware update failed for agentId: " + agentId));

        // Failure -> setUpdateRequested(agentId, true)
        verify(agentService).setUpdateRequested(agentId, true);

        // Make sure we do NOT call setCurrentFirmwareAfterUpdate
        verify(agentService, never()).setCurrentFirmwareAfterUpdate(anyString());
    }

    @Test
    void testUpdateAgentFirmwareStatus_UnknownStatus() throws Exception {
        String agentId = "agent123";
        String status = "whatever";

        Agent agent = new Agent();
        agent.setAgentId(agentId);
        when(agentService.getAgentByAgentId(agentId)).thenReturn(Optional.of(agent));

        Map<String, Object> body = new HashMap<>();
        body.put("status", status);
        body.put("deviceId", "SimulatedDevice123");

        mockMvc.perform(put("/agents/{agentId}/update-status", agentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(body))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Unknown status value: " + status));

        // No call to setUpdateRequested(...) or setCurrentFirmwareAfterUpdate(...)
        verify(agentService, never()).setUpdateRequested(anyString(), anyBoolean());
        verify(agentService, never()).setCurrentFirmwareAfterUpdate(anyString());
    }

    @Test
    void testUpdateAgentFirmwareStatus_AgentNotFound() throws Exception {
        String agentId = "missingAgent";

        // The service returns Optional.empty() or throws an exception
        when(agentService.getAgentByAgentId(agentId)).thenReturn(Optional.empty());

        Map<String, Object> body = new HashMap<>();
        body.put("status", "success");
        body.put("deviceId", "TestDevice");

        mockMvc.perform(put("/agents/{agentId}/update-status", agentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(body))
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Could not update firmware status"));

        // We can't even get the agent, so no further calls
        verify(agentService).getAgentByAgentId(agentId);
        verify(agentService, never()).setUpdateRequested(anyString(), anyBoolean());
        verify(agentService, never()).setCurrentFirmwareAfterUpdate(anyString());
    }

    @Test
    void testUpdateAgentFirmwareStatus_ServiceException() throws Exception {
        String agentId = "agent123";

        // We'll simulate an agent is found,
        // but a DB error occurs (e.g. in setUpdateRequested or setCurrentFirmwareAfterUpdate)
        Agent agent = new Agent();
        agent.setAgentId(agentId);
        when(agentService.getAgentByAgentId(agentId)).thenReturn(Optional.of(agent));

        // Throw an exception in setUpdateRequested
        doThrow(new RuntimeException("DB Error")).when(agentService).setUpdateRequested(agentId, false);

        Map<String, Object> body = new HashMap<>();
        body.put("status", "success");
        body.put("deviceId", "SimulatedDevice");

        mockMvc.perform(put("/agents/{agentId}/update-status", agentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(body))
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Could not update firmware status"));

        verify(agentService).getAgentByAgentId(agentId);
        // The method tried setUpdateRequested(agentId, false) and threw
        verify(agentService).setUpdateRequested(agentId, false);
        // setCurrentFirmwareAfterUpdate was never reached
        verify(agentService, never()).setCurrentFirmwareAfterUpdate(agentId);
    }

    @Test
    void testCreateTag_Success() throws Exception {
        // Arrange
        String tagName = "TestTag";

        // The JSON body we send
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", tagName);

        // We don't need a return from categoryRepository.save(...),
        // but we should verify it's called. We'll just do a stub:
        when(categoryRepository.save(any(AgentCategory.class))).thenAnswer(invocation -> {
            AgentCategory saved = invocation.getArgument(0);
            saved.setId(1L); // Pretend the DB assigned ID 1
            return saved;
        });

        // Act & Assert
        mockMvc.perform(post("/agents/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestBody))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Tag '" + tagName + "' created successfully."));

        // Verify that categoryRepository.save(...) was called once
        verify(categoryRepository, times(1)).save(any(AgentCategory.class));
    }

    @Test
    void testCreateTag_BlankName() throws Exception {
        // Arrange
        // The JSON body with blank or missing name
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", "");

        // Act & Assert
        mockMvc.perform(post("/agents/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestBody))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Tag name cannot be empty."));

        // Verify that categoryRepository.save(...) was never called
        verify(categoryRepository, never()).save(any(AgentCategory.class));
    }

    @Test
    void testCreateTag_NameIsNull() throws Exception {
        // Arrange
        Map<String, String> requestBody = new HashMap<>();
        // no "name" key at all, or we do put("name", null)

        // Act & Assert
        mockMvc.perform(post("/agents/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestBody))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Tag name cannot be empty."));

        verify(categoryRepository, never()).save(any(AgentCategory.class));
    }
}

