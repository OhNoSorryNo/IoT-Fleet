package IoTFleetManagment.test.user.service;

import IoTFleetManagement.common.util.TokenGenerator;
import IoTFleetManagement.user.model.AdminInvitation;
import IoTFleetManagement.user.repository.AdminInvitationRepository;
import IoTFleetManagement.user.service.AdminInvitationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminInvitationServiceTest {

    @Mock
    private AdminInvitationRepository invitationRepository;

    @InjectMocks
    private AdminInvitationService adminInvitationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateInvitationToken() {
        // Arrange
        String generatedToken = "sample-token";
        LocalDateTime expirationTime = LocalDateTime.now().plusDays(1);

        // Mock the static method
        try (MockedStatic<TokenGenerator> mockedStatic = mockStatic(TokenGenerator.class)) {
            mockedStatic.when(TokenGenerator::generateToken).thenReturn(generatedToken);

            AdminInvitation mockInvitation = new AdminInvitation();
            mockInvitation.setToken(generatedToken);
            mockInvitation.setExpiresAt(expirationTime);

            when(invitationRepository.save(any(AdminInvitation.class))).thenReturn(mockInvitation);

            // Act
            AdminInvitation createdInvitation = adminInvitationService.createInvitationToken();

            // Assert
            assertNotNull(createdInvitation, "Created invitation should not be null");
            assertEquals(generatedToken, createdInvitation.getToken(), "Token should match the generated token");
            assertNotNull(createdInvitation.getExpiresAt(), "Expiration time should be set");
            assertTrue(createdInvitation.getExpiresAt().isAfter(LocalDateTime.now()), "Expiration time should be in the future");
            verify(invitationRepository, times(1)).save(any(AdminInvitation.class));

            mockedStatic.verify(TokenGenerator::generateToken, times(1)); // Verify static method was called
        }
    }

    @Test
    void testValidateTokenValid() {
        // Arrange
        String token = "valid-token";
        AdminInvitation mockInvitation = new AdminInvitation();
        mockInvitation.setToken(token);
        mockInvitation.setUsed(false);
        mockInvitation.setExpiresAt(LocalDateTime.now().plusDays(1));

        when(invitationRepository.findByToken(token)).thenReturn(Optional.of(mockInvitation));

        // Act
        Optional<AdminInvitation> result = adminInvitationService.validateToken(token);

        // Assert
        assertTrue(result.isPresent(), "Token should be valid");
        assertEquals(mockInvitation, result.get(), "Returned invitation should match the mock invitation");
        verify(invitationRepository, times(1)).findByToken(token);
    }

    @Test
    void testValidateTokenExpired() {
        // Arrange
        String token = "expired-token";
        AdminInvitation mockInvitation = new AdminInvitation();
        mockInvitation.setToken(token);
        mockInvitation.setUsed(false);
        mockInvitation.setExpiresAt(LocalDateTime.now().minusHours(1));

        when(invitationRepository.findByToken(token)).thenReturn(Optional.of(mockInvitation));

        // Act
        Optional<AdminInvitation> result = adminInvitationService.validateToken(token);

        // Assert
        assertTrue(result.isEmpty(), "Token should be invalid due to expiration");
        verify(invitationRepository, times(1)).findByToken(token);
    }

    @Test
    void testValidateTokenAlreadyUsed() {
        // Arrange
        String token = "used-token";
        AdminInvitation mockInvitation = new AdminInvitation();
        mockInvitation.setToken(token);
        mockInvitation.setUsed(true);
        mockInvitation.setExpiresAt(LocalDateTime.now().plusHours(1));

        when(invitationRepository.findByToken(token)).thenReturn(Optional.of(mockInvitation));

        // Act
        Optional<AdminInvitation> result = adminInvitationService.validateToken(token);

        // Assert
        assertTrue(result.isEmpty(), "Token should be invalid because it is already used");
        verify(invitationRepository, times(1)).findByToken(token);
    }

    @Test
    void testMarkTokenAsUsed() {
        // Arrange
        AdminInvitation mockInvitation = new AdminInvitation();
        mockInvitation.setId(1L);
        mockInvitation.setToken("valid-token");
        mockInvitation.setUsed(false);

        when(invitationRepository.findById(mockInvitation.getId())).thenReturn(Optional.of(mockInvitation));
        when(invitationRepository.save(any(AdminInvitation.class))).thenReturn(mockInvitation);

        // Act
        adminInvitationService.markTokenAsUsed(mockInvitation);

        // Assert
        verify(invitationRepository, times(1)).findById(mockInvitation.getId());
        verify(invitationRepository, times(1)).save(mockInvitation);
        assertTrue(mockInvitation.isUsed(), "Invitation should be marked as used");
        assertNotNull(mockInvitation.getUsedAt(), "UsedAt timestamp should be set");
    }

    @Test
    void testMarkTokenAsUsedInvitationNotFound() {
        // Arrange
        AdminInvitation mockInvitation = new AdminInvitation();
        mockInvitation.setId(1L);

        when(invitationRepository.findById(mockInvitation.getId())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            adminInvitationService.markTokenAsUsed(mockInvitation);
        });
        assertEquals("Invitation not found", exception.getMessage(), "Exception message should match");
        verify(invitationRepository, times(1)).findById(mockInvitation.getId());
    }
}
