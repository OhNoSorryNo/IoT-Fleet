package IoTFleetManagment.test.agent.service;

import IoTFleetManagement.agent.model.Agent;
import IoTFleetManagement.agent.model.AgentCategory;
import IoTFleetManagement.agent.repository.AgentCategoryRepository;
import IoTFleetManagement.agent.repository.AgentRepository;
import IoTFleetManagement.agent.service.AgentService;
import IoTFleetManagement.common.exceptions.AlreadyExistsException;
import IoTFleetManagement.firmware.model.FirmwareVersion;
import IoTFleetManagement.firmware.repository.FirmwareVersionRepository;
import IoTFleetManagement.security.config.JwtUtil;
import IoTFleetManagement.user.model.User;
import IoTFleetManagement.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import javax.naming.AuthenticationException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AgentServiceTest {

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil; // For mocking instance methods

    @Mock
    private FirmwareVersionRepository firmwareVersionRepository;

    @Mock
    private AgentCategoryRepository categoryRepository;

    @InjectMocks
    private AgentService agentService;

    @BeforeAll
    static void setupEnvironment() {
        // Set the SECRET_TOKEN to satisfy JwtUtil static initialization
        System.setProperty("SECRET_TOKEN", "abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ1234");
    }

    @BeforeEach
    void setUp() {
        // After Mockito does its injection, still inject the missing field:
        ReflectionTestUtils.setField(
                agentService,
                "firmwareVersionRepository",
                firmwareVersionRepository
        );
    }

    @Test
    public void testGenerateToken() {
        // Arrange
        String subject = "test-agent-id";

        // Act
        String token = JwtUtil.generateToken(subject);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testGetAllAgents() {
        // Arrange: Prepare a list of mock agents
        Agent agent1 = new Agent();
        agent1.setAgentId("agent1");
        Agent agent2 = new Agent();
        agent2.setAgentId("agent2");

        // Mock the repository to return the prepared list
        when(agentRepository.findAll()).thenReturn(Arrays.asList(agent1, agent2));

        // Act: Call the service method
        List<Agent> agents = agentService.getAllAgents();

        // Assert: Validate the results
        assertNotNull(agents); // Ensure the list is not null
        assertEquals(2, agents.size()); // Validate the size of the list
        assertEquals("agent1", agents.get(0).getAgentId()); // Validate first agent
        assertEquals("agent2", agents.get(1).getAgentId()); // Validate second agent

        // Verify that the repository's findAll method was called once
        verify(agentRepository, times(1)).findAll();
    }


    // Test case for getAgentStatus when agent is found
    @Test
    public void testGetAgentStatus_Found() throws Exception {
        // Arrange
        Agent agent = new Agent();
        agent.setAgentId("agent1");
        agent.setOnline(true);

        when(agentRepository.findByAgentId("agent1")).thenReturn(Optional.of(agent));

        // Act
        boolean status = agentService.getAgentStatus("agent1");

        // Assert
        assertTrue(status);
        verify(agentRepository, times(1)).findByAgentId("agent1");
    }

    // Test case for getAgentStatus when agent is not found
    @Test
    public void testGetAgentStatus_NotFound() {
        // Arrange
        when(agentRepository.findByAgentId("agent1")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ChangeSetPersister.NotFoundException.class, () -> agentService.getAgentStatus("agent1"));
        verify(agentRepository, times(1)).findByAgentId("agent1");
    }

    // Test case for addAgent with successful addition
    @Test
    public void testAddAgent_Success() {
        // Arrange
        Agent agent = new Agent();
        agent.setAgentId("agent1");
        agent.setSecretKey("rawSecret");

        when(agentRepository.findByAgentId("agent1")).thenReturn(Optional.empty());
        when(agentRepository.save(any(Agent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Agent savedAgent = agentService.addAgent(agent);

        // Assert
        assertNotNull(savedAgent);
        assertNotEquals("rawSecret", savedAgent.getSecretKey()); // Ensure the secret is hashed
        verify(agentRepository, times(1)).findByAgentId("agent1");
        verify(agentRepository, times(1)).save(any(Agent.class));
    }

    // Test case for addAgent when agentId already exists
    @Test
    public void testAddAgent_AlreadyExists() {
        // Arrange
        Agent existingAgent = new Agent();
        existingAgent.setAgentId("agent1");

        when(agentRepository.findByAgentId("agent1")).thenReturn(Optional.of(existingAgent));

        Agent newAgent = new Agent();
        newAgent.setAgentId("agent1");
        newAgent.setSecretKey("rawSecret");

        // Act & Assert
        assertThrows(AlreadyExistsException.class, () -> agentService.addAgent(newAgent));
        verify(agentRepository, times(1)).findByAgentId("agent1");
        verify(agentRepository, never()).save(any(Agent.class));
    }

    // Test case for addAgent when agentId is blank
    @Test
    public void testAddAgent_InvalidAgentId() {
        // Arrange
        Agent invalidAgent = new Agent();
        invalidAgent.setAgentId("");
        invalidAgent.setSecretKey("rawSecret");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> agentService.addAgent(invalidAgent));
        verify(agentRepository, never()).save(any(Agent.class));
    }

    @Test
    void testAgentExists_ReturnsTrue() {
        // Arrange
        when(agentRepository.findByAgentId("agent123"))
                .thenReturn(Optional.of(new Agent())); // found an agent

        // Act
        boolean exists = agentService.agentExists("agent123");

        // Assert
        assertTrue(exists);
        verify(agentRepository, times(1)).findByAgentId("agent123");
    }

    @Test
    void testAgentExists_ReturnsFalse() {
        // Arrange
        when(agentRepository.findByAgentId("agent123"))
                .thenReturn(Optional.empty()); // no agent found

        // Act
        boolean exists = agentService.agentExists("agent123");

        // Assert
        assertFalse(exists);
        verify(agentRepository, times(1)).findByAgentId("agent123");
    }

    // Test case for updateAgentStatus when agent is found
    @Test
    public void testUpdateAgentStatus_Success() throws Exception {
        // Arrange
        Agent agent = new Agent();
        agent.setAgentId("agent1");
        agent.setOnline(false);

        when(agentRepository.findByAgentId("agent1")).thenReturn(Optional.of(agent));
        when(agentRepository.save(any(Agent.class))).thenReturn(agent);

        // Act
        agentService.updateAgentStatus("agent1", true);

        // Assert
        assertTrue(agent.isOnline());
        verify(agentRepository, times(1)).findByAgentId("agent1");
        verify(agentRepository, times(1)).save(agent);
    }

    // Test case for updateAgentStatus when agent is not found
    @Test
    public void testUpdateAgentStatus_NotFound() {
        // Arrange
        when(agentRepository.findByAgentId("agent1")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ChangeSetPersister.NotFoundException.class, () -> agentService.updateAgentStatus("agent1", true));
        verify(agentRepository, times(1)).findByAgentId("agent1");
        verify(agentRepository, never()).save(any(Agent.class));
    }

    @Test
    public void testAssignAgentToUser_Success() {
        // Arrange
        Agent agent = new Agent();
        agent.setId(1L);

        User user = new User();
        user.setId(2L);

        when(agentRepository.findById(1L)).thenReturn(Optional.of(agent));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(agentRepository.save(any(Agent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Agent assignedAgent = agentService.assignAgentToUser(1L, 2L);

        // Assert
        assertNotNull(assignedAgent);
        assertEquals(user, assignedAgent.getUser());
        verify(agentRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(2L);
        verify(agentRepository, times(1)).save(agent);
    }

    @Test
    public void testAssignAgentToUser_AgentNotFound() {
        // Arrange
        when(agentRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> agentService.assignAgentToUser(1L, 2L));
        verify(agentRepository, times(1)).findById(1L);
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    public void testAssignAgentToUser_UserNotFound() {
        // Arrange
        when(agentRepository.findById(1L)).thenReturn(Optional.of(new Agent())); // Agent exists
        when(userRepository.findById(2L)).thenReturn(Optional.empty()); // User does not exist

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> agentService.assignAgentToUser(1L, 2L));
        assertEquals("User not found", exception.getMessage());

        verify(agentRepository, times(1)).findById(1L); // Ensure the agent repository was queried
        verify(userRepository, times(1)).findById(2L); // Ensure the user repository was queried
        verify(agentRepository, never()).save(any(Agent.class)); // Ensure no agent was saved
    }

    @Test
    public void testRegisterAgentToUser_Success() {
        // Arrange
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String rawSecret = "rawSecret";
        String hashedSecret = passwordEncoder.encode(rawSecret); // Properly hash the secret

        Agent agent = new Agent();
        agent.setSecretKey(hashedSecret); // Use the hashed secret

        User user = new User();
        user.setId(2L);

        when(agentRepository.findAll()).thenReturn(List.of(agent));
        when(agentRepository.save(any(Agent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Agent registeredAgent = agentService.registerAgentToUser(rawSecret, user);

        // Assert
        assertNotNull(registeredAgent);
        assertEquals(user, registeredAgent.getUser());
        verify(agentRepository, times(1)).findAll();
        verify(agentRepository, times(1)).save(agent);
    }

    @Test
    public void testRegisterAgentToUser_AgentNotFound() {
        // Arrange
        when(agentRepository.findAll()).thenReturn(List.of()); // No agents in the repository

        User user = new User();
        user.setId(1L);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> agentService.registerAgentToUser("invalidSecret", user));
        assertEquals("Invalid secret key", exception.getMessage());

        verify(agentRepository, times(1)).findAll();
        verify(agentRepository, never()).save(any(Agent.class));
    }

    @Test
    public void testRegisterAgentToUser_AlreadyAssigned() {
        // Arrange
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String rawSecret = "rawSecret";
        String hashedSecret = passwordEncoder.encode(rawSecret);

        Agent agent = new Agent();
        agent.setSecretKey(hashedSecret);
        agent.setUser(new User()); // Agent already assigned to a user

        when(agentRepository.findAll()).thenReturn(List.of(agent));

        User newUser = new User();
        newUser.setId(2L);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> agentService.registerAgentToUser(rawSecret, newUser));
        assertEquals("This agent is already registered to another user", exception.getMessage());

        verify(agentRepository, times(1)).findAll();
        verify(agentRepository, never()).save(any(Agent.class));
    }

    @Test
    public void testRegisterAgentToUser_InvalidSecretKey() {
        // Arrange
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String hashedSecret = passwordEncoder.encode("validSecret");

        Agent agent = new Agent();
        agent.setSecretKey(hashedSecret);

        when(agentRepository.findAll()).thenReturn(List.of(agent));

        User user = new User();
        user.setId(2L);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> agentService.registerAgentToUser("invalidSecret", user));
        assertEquals("Invalid secret key", exception.getMessage());

        verify(agentRepository, times(1)).findAll();
        verify(agentRepository, never()).save(any(Agent.class));
    }

    @Test
    public void testValidateToken() {
        // Arrange
        when(jwtUtil.validateToken("validToken", "agent1")).thenReturn(true);

        // Act
        boolean isValid = agentService.validateToken("validToken", "agent1");

        // Assert
        assertTrue(isValid);
        verify(jwtUtil, times(1)).validateToken("validToken", "agent1");
    }
    @Test
    public void testAuthenticate_Success() throws Exception {
        // Arrange
        Agent agent = new Agent();
        agent.setAgentId("agent1");
        agent.setSecretKey(new BCryptPasswordEncoder().encode("validSecret"));

        when(agentRepository.findByAgentId("agent1")).thenReturn(Optional.of(agent));

        // Act
        Agent authenticatedAgent = agentService.authenticate("agent1", "validSecret");

        // Assert
        assertNotNull(authenticatedAgent);
        assertEquals("agent1", authenticatedAgent.getAgentId());
        verify(agentRepository, times(1)).findByAgentId("agent1");
    }

    @Test
    public void testAuthenticate_InvalidSecretKey() throws Exception {
        // Arrange
        Agent agent = new Agent();
        agent.setAgentId("agent1");
        agent.setSecretKey(new BCryptPasswordEncoder().encode("validSecret"));

        when(agentRepository.findByAgentId("agent1")).thenReturn(Optional.of(agent));

        // Act & Assert
        assertThrows(AuthenticationException.class, () -> agentService.authenticate("agent1", "invalidSecret"));
        verify(agentRepository, times(1)).findByAgentId("agent1");
    }

    @Test
    public void testAuthenticate_AgentNotFound() {
        // Arrange
        when(agentRepository.findByAgentId("agent1")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ChangeSetPersister.NotFoundException.class, () -> agentService.authenticate("agent1", "secret"));
        verify(agentRepository, times(1)).findByAgentId("agent1");
    }

    @Test
    public void testCheckAllDevicesStatus() {
        // Arrange
        Agent onlineAgent = new Agent();
        onlineAgent.setAgentId("agent1");
        onlineAgent.setLastSeen(LocalDateTime.now().minusSeconds(61)); // Last seen over 60 seconds ago
        onlineAgent.setOnline(true);

        Agent recentAgent = new Agent();
        recentAgent.setAgentId("agent2");
        recentAgent.setLastSeen(LocalDateTime.now().minusSeconds(30)); // Last seen recently
        recentAgent.setOnline(true);

        when(agentRepository.findByLastSeenBeforeAndOnline(any(LocalDateTime.class), eq(true)))
                .thenReturn(List.of(onlineAgent));

        // Act
        agentService.checkAllDevicesStatus();

        // Assert
        assertFalse(onlineAgent.isOnline()); // Should be set to offline
        verify(agentRepository, times(1)).findByLastSeenBeforeAndOnline(any(LocalDateTime.class), eq(true));
        verify(agentRepository, times(1)).save(onlineAgent);
        verify(agentRepository, never()).save(recentAgent);
    }

    @Test
    public void testGetAgentByAgentId_Found() {
        // Arrange
        Agent agent = new Agent();
        agent.setAgentId("agent1");

        when(agentRepository.findByAgentId("agent1")).thenReturn(Optional.of(agent));

        // Act
        Optional<Agent> result = agentService.getAgentByAgentId("agent1");

        // Assert
        assertTrue(result.isPresent()); // Ensure the result is present
        assertEquals("agent1", result.get().getAgentId()); // Check the agentId
        verify(agentRepository, times(1)).findByAgentId("agent1"); // Verify the repository method was called
    }

    @Test
    public void testGetAgentByAgentId_NotFound() {
        // Arrange
        when(agentRepository.findByAgentId("agent1")).thenReturn(Optional.empty());

        // Act
        Optional<Agent> result = agentService.getAgentByAgentId("agent1");

        // Assert
        assertFalse(result.isPresent()); // Ensure the result is empty
        verify(agentRepository, times(1)).findByAgentId("agent1"); // Verify the repository method was called
    }

    @Test
    public void testRemoveUserFromAgent() {
        // Arrange
        Agent agent = new Agent();
        agent.setAgentId("agent1");
        User user = new User();
        user.setId(1L);
        agent.setUser(user); // Initially, the agent is assigned to a user

        when(agentRepository.save(any(Agent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Agent updatedAgent = agentService.removeUserFromAgent(agent);

        // Assert
        assertNotNull(updatedAgent); // Ensure the returned agent is not null
        assertNull(updatedAgent.getUser()); // Verify the user is removed
        assertEquals("agent1", updatedAgent.getAgentId()); // Ensure the agentId remains the same
        verify(agentRepository, times(1)).save(agent); // Ensure the repository's save method was called once
    }

    @Test
    public void testSaveAgent() {
        // Arrange
        Agent agent = new Agent();
        agent.setAgentId("agent1");

        when(agentRepository.save(any(Agent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Agent savedAgent = agentService.saveAgent(agent);

        // Assert
        assertNotNull(savedAgent); // Ensure the returned agent is not null
        assertEquals("agent1", savedAgent.getAgentId()); // Verify the agentId is correctly saved
        verify(agentRepository, times(1)).save(agent); // Ensure the repository's save method was called exactly once
    }

    @Test
    void testAssignFirmwareToAgent_Success() {
        // Arrange
        Long agentId = 1L;
        Long firmwareId = 2L;

        Agent agent = new Agent();
        agent.setId(agentId);

        FirmwareVersion firmwareVersion = new FirmwareVersion();
        firmwareVersion.setId(firmwareId);

        when(agentRepository.findById(agentId)).thenReturn(Optional.of(agent));
        when(firmwareVersionRepository.findById(firmwareId)).thenReturn(Optional.of(firmwareVersion));
        when(agentRepository.save(agent)).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Agent updatedAgent = agentService.assignFirmwareToAgent(agentId, firmwareId);

        // Assert
        assertNotNull(updatedAgent);
        assertEquals(firmwareVersion, updatedAgent.getNewFirmware(),
                "The agent's new firmware should match the requested firmware version");
        verify(agentRepository).findById(agentId);
        verify(firmwareVersionRepository).findById(firmwareId);
        verify(agentRepository).save(agent);
    }

    @Test
    void testAssignFirmwareToAgent_AgentNotFound() {
        // Arrange
        Long agentId = 1L;
        Long firmwareId = 2L;

        when(agentRepository.findById(agentId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> agentService.assignFirmwareToAgent(agentId, firmwareId));

        assertEquals("Agent not found", exception.getMessage());
        verify(agentRepository).findById(agentId);
        verify(firmwareVersionRepository, never()).findById(anyLong());
        verify(agentRepository, never()).save(any(Agent.class));
    }

    @Test
    void testAssignFirmwareToAgent_FirmwareNotFound() {
        // Arrange
        Long agentId = 1L;
        Long firmwareId = 2L;

        Agent agent = new Agent();
        agent.setId(agentId);

        when(agentRepository.findById(agentId)).thenReturn(Optional.of(agent));
        when(firmwareVersionRepository.findById(firmwareId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> agentService.assignFirmwareToAgent(agentId, firmwareId));

        assertEquals("Firmware version not found", exception.getMessage());
        verify(agentRepository).findById(agentId);
        verify(firmwareVersionRepository).findById(firmwareId);
        verify(agentRepository, never()).save(any(Agent.class));
    }

    @Test
    void testAssignCategoriesToAgent_Success() {
        // Arrange
        Long agentId = 1L;
        List<Long> categoryIds = List.of(10L, 20L);

        Agent agent = new Agent();
        agent.setId(agentId);

        AgentCategory category1 = new AgentCategory();
        category1.setId(10L);
        AgentCategory category2 = new AgentCategory();
        category2.setId(20L);

        List<AgentCategory> foundCategories = List.of(category1, category2);

        // Mock agentRepository to find an Agent
        when(agentRepository.findById(agentId)).thenReturn(Optional.of(agent));
        // Mock categoryRepository to return categories
        when(categoryRepository.findAllById(categoryIds)).thenReturn(foundCategories);

        // Act
        agentService.assignCategoriesToAgent(agentId, categoryIds);

        // Assert
        // The agent should now have these categories
        assertEquals(2, agent.getCategories().size());
        assertTrue(agent.getCategories().contains(category1));
        assertTrue(agent.getCategories().contains(category2));

        // Ensure we saved the agent after assigning categories
        verify(agentRepository).save(agent);
    }

    @Test
    void testAssignCategoriesToAgent_AgentNotFound() {
        // Arrange
        Long agentId = 1L;
        List<Long> categoryIds = List.of(10L, 20L);

        // Mock agentRepository to return empty
        when(agentRepository.findById(agentId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> agentService.assignCategoriesToAgent(agentId, categoryIds));
        assertEquals("Agent not found", ex.getMessage());

        // Ensure categoryRepository and agentRepository.save(...) never get called
        verify(categoryRepository, never()).findAllById(anyList());
        verify(agentRepository, never()).save(any(Agent.class));
    }

    @Test
    void testAssignCategoriesToAgent_InvalidCategoryIds() {
        // Arrange
        Long agentId = 1L;
        List<Long> categoryIds = List.of(10L, 20L);

        Agent agent = new Agent();
        agent.setId(agentId);

        // Mock agentRepository to find an Agent
        when(agentRepository.findById(agentId)).thenReturn(Optional.of(agent));
        // Mock categoryRepository to return an empty list
        when(categoryRepository.findAllById(categoryIds)).thenReturn(List.of());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> agentService.assignCategoriesToAgent(agentId, categoryIds));
        assertEquals("Invalid category IDs provided", ex.getMessage());

        // Verify we do not save the agent if no categories are found
        verify(agentRepository, never()).save(any(Agent.class));
    }

    @Test
    void testFindAgentsByCategory_MatchingAgents() {
        // Arrange
        // Create two agents with different categories
        Agent agent1 = new Agent();
        AgentCategory cat1 = new AgentCategory();
        cat1.setName("CategoryA");
        agent1.setCategories(Collections.singleton(cat1));

        Agent agent2 = new Agent();
        AgentCategory cat2 = new AgentCategory();
        cat2.setName("CategoryB");
        agent2.setCategories(Collections.singleton(cat2));

        // Suppose the repository returns both agents
        when(agentRepository.findAll()).thenReturn(List.of(agent1, agent2));

        // Act
        List<Agent> result = agentService.findAgentsByCategory("CategoryA");

        // Assert
        // Only agent1 has "CategoryA"
        assertEquals(1, result.size(), "Exactly one agent should match CategoryA");
        assertSame(agent1, result.get(0), "The matching agent should be agent1");
        verify(agentRepository, times(1)).findAll();
    }

    @Test
    void testFindAgentsByCategory_NoMatches() {
        // Arrange
        // Create one agent with a single category "CategoryA"
        Agent agent1 = new Agent();
        AgentCategory cat1 = new AgentCategory();
        cat1.setName("CategoryA");
        agent1.setCategories(Collections.singleton(cat1));

        // Suppose the repository returns agent1
        when(agentRepository.findAll()).thenReturn(List.of(agent1));

        // Act
        List<Agent> result = agentService.findAgentsByCategory("CategoryB");

        // Assert
        // No agent has "CategoryB", so we expect an empty result
        assertTrue(result.isEmpty(), "No agents should match CategoryB");
        verify(agentRepository, times(1)).findAll();
    }

    @Test
    void testIsUpdateAgentUpdateNeeded_AgentFound_True() {
        // Arrange
        String agentId = "agent123";
        Agent agent = new Agent();
        agent.setAgentId(agentId);
        agent.setUpdateRequested(true);

        when(agentRepository.findByAgentId(agentId)).thenReturn(Optional.of(agent));

        // Act
        boolean result = agentService.isUpdateAgentUpdateNeeded(agentId);

        // Assert
        assertTrue(result);
        verify(agentRepository).findByAgentId(agentId);
    }

    @Test
    void testIsUpdateAgentUpdateNeeded_AgentFound_False() {
        // Arrange
        String agentId = "agent456";
        Agent agent = new Agent();
        agent.setAgentId(agentId);
        agent.setUpdateRequested(false);

        when(agentRepository.findByAgentId(agentId)).thenReturn(Optional.of(agent));

        // Act
        boolean result = agentService.isUpdateAgentUpdateNeeded(agentId);

        // Assert
        assertFalse(result);
        verify(agentRepository).findByAgentId(agentId);
    }

    @Test
    void testIsUpdateAgentUpdateNeeded_AgentNotFound() {
        // Arrange
        String agentId = "missingAgent";
        when(agentRepository.findByAgentId(agentId)).thenReturn(Optional.empty());

        // Act
        boolean result = agentService.isUpdateAgentUpdateNeeded(agentId);

        // Assert
        assertFalse(result);
        verify(agentRepository).findByAgentId(agentId);
    }

    @Test
    void testSetUpdateRequested_Success() {
        // Arrange
        String agentId = "agent123";
        Agent agent = new Agent();
        agent.setAgentId(agentId);
        agent.setUpdateRequested(false);

        when(agentRepository.findByAgentId(agentId)).thenReturn(Optional.of(agent));

        // Act
        agentService.setUpdateRequested(agentId, true);

        // Assert
        assertTrue(agent.isUpdateRequested(),
                "updateRequested should be set to true on the agent");
        verify(agentRepository).findByAgentId(agentId);
        verify(agentRepository).save(agent);
    }

    @Test
    void testSetUpdateRequested_AgentNotFound() {
        // Arrange
        String agentId = "notFoundId";
        when(agentRepository.findByAgentId(agentId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> agentService.setUpdateRequested(agentId, true));
        assertEquals("Agent not found with ID: " + agentId, ex.getMessage());

        verify(agentRepository).findByAgentId(agentId);
        // Ensure we never call save if the agent is missing
        verify(agentRepository, never()).save(any(Agent.class));
    }

    @Test
    void testSetCurrentFirmwareAfterUpdate_Success() {
        // Arrange
        String agentId = "agent789";
        Agent agent = new Agent();
        agent.setAgentId(agentId);

        FirmwareVersion oldFirmware = new FirmwareVersion();
        oldFirmware.setId(1L);

        FirmwareVersion newFirmware = new FirmwareVersion();
        newFirmware.setId(2L);

        agent.setFirmwareVersion(oldFirmware);
        agent.setNewFirmware(newFirmware);

        when(agentRepository.findByAgentId(agentId)).thenReturn(Optional.of(agent));

        // Act
        agentService.setCurrentFirmwareAfterUpdate(agentId);

        // Assert
        assertEquals(newFirmware, agent.getFirmwareVersion(),
                "agent's firmwareVersion should be updated to newFirmware");
        verify(agentRepository).findByAgentId(agentId);
        verify(agentRepository).save(agent);
    }

    @Test
    void testSetCurrentFirmwareAfterUpdate_AgentNotFound() {
        // Arrange
        String agentId = "missingAgent";
        when(agentRepository.findByAgentId(agentId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> agentService.setCurrentFirmwareAfterUpdate(agentId));
        assertEquals("Agent not found with ID: " + agentId, ex.getMessage());

        verify(agentRepository).findByAgentId(agentId);
        // Ensure we don't call save if agent is missing
        verify(agentRepository, never()).save(any(Agent.class));
    }



}
