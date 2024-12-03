package IoTFleetmanagement.test.agent.service;

import IoTFleetManagement.agent.model.Agent;
import IoTFleetManagement.agent.repository.AgentRepository;
import IoTFleetManagement.agent.service.AgentService;
import IoTFleetManagement.common.exceptions.AlreadyExistsException;
import IoTFleetManagement.security.config.JwtUtil;
import IoTFleetManagement.user.model.User;
import IoTFleetManagement.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.naming.AuthenticationException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AgentServiceTest {

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AgentService agentService;

    // Test case for getAllAgents
    @Test
    public void testGetAllAgents() {
        // Arrange
        Agent agent1 = new Agent();
        agent1.setAgentId("agent1");
        Agent agent2 = new Agent();
        agent2.setAgentId("agent2");

        when(agentRepository.findAll()).thenReturn(Arrays.asList(agent1, agent2));

        // Act
        var agents = agentService.getAllAgents();

        // Assert
        assertEquals(2, agents.size());
        assertEquals("agent1", agents.get(0).getAgentId());
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


}
