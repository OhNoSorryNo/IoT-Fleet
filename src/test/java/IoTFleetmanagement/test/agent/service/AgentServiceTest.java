package IoTFleetmanagement.test.agent.service;

import IoTFleetManagement.agent.model.Agent;
import IoTFleetManagement.agent.repository.AgentRepository;
import IoTFleetManagement.agent.service.AgentService;
import IoTFleetManagement.common.exceptions.AlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.crossstore.ChangeSetPersister;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AgentServiceTest {

    @Mock
    private AgentRepository agentRepository;

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
}
