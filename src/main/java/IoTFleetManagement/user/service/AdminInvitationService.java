package IoTFleetManagement.user.service;

import IoTFleetManagement.common.exceptions.util.TokenGenerator;
import IoTFleetManagement.user.model.AdminInvitation;
import IoTFleetManagement.user.repository.AdminInvitationRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service class for managing admin invitation tokens.
 * <p>
 * This service provides the business logic for creating, validating, and managing
 * the lifecycle of admin invitation tokens. These tokens are used to securely
 * register new admin users in the system.
 * </p>
 *
 * <p><b>Key Responsibilities:</b></p>
 * <ul>
 *   <li>Generate unique invitation tokens with expiration.</li>
 *   <li>Validate tokens to ensure they are unused and not expired.</li>
 *   <li>Mark tokens as used after successful registration.</li>
 * </ul>
 *
 * <p>This service interacts with the {@link AdminInvitationRepository} to perform database operations.</p>
 *
 * @author jasmin1707
 * @author Lara
 */
@Service
public class AdminInvitationService {

    @Autowired
    private AdminInvitationRepository invitationRepository;

    /**
     * Creates a new admin invitation token.
     * <p>
     * This method generates a unique token using {@link TokenGenerator}, sets its expiration
     * time to 24 hours from creation, and saves it to the database.
     * </p>
     *
     * @return The created {@link AdminInvitation} containing the token and metadata.
     */
    public AdminInvitation createInvitationToken() {
        String token = TokenGenerator.generateToken();
        AdminInvitation invitation = new AdminInvitation();
        invitation.setToken(token);
        invitation.setExpiresAt(LocalDateTime.now().plusDays(1)); // Token valid for 24 hours
        return invitationRepository.save(invitation);
    }

    /**
     * Validates an admin invitation token.
     * <p>
     * This method checks if the provided token exists, has not been used, and has not expired.
     * It is used during the admin registration process to ensure the validity of the token.
     * </p>
     *
     * @param token The invitation token to validate.
     * @return An {@link Optional} containing the valid {@link AdminInvitation} if the token is valid,
     *         or an empty {@link Optional} if the token is invalid, expired, or already used.
     */
    public Optional<AdminInvitation> validateToken(String token) {
        return invitationRepository.findByToken(token)
                .filter(invitation -> !invitation.isUsed())
                .filter(invitation -> invitation.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    /**
     * Marks an admin invitation token as used.
     * <p>
     * This method updates the status of the provided invitation token, marking it as used,
     * and records the timestamp of when it was used. It then saves the updated invitation
     * to the database.
     * </p>
     *
     * @param invitation The {@link AdminInvitation} to mark as used.
     */
    @Transactional
    public void markTokenAsUsed(AdminInvitation invitation) {
        AdminInvitation existingInvitation = invitationRepository.findById(invitation.getId())
                .orElseThrow(() -> new IllegalStateException("Invitation not found"));
        existingInvitation.setUsed(true);
        existingInvitation.setUsedAt(LocalDateTime.now());
        invitationRepository.save(existingInvitation);
    }
}
