package IoTFleetManagement.user.controller;

import IoTFleetManagement.user.model.AdminInvitation;
import IoTFleetManagement.user.service.AdminInvitationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing admin invitation tokens.
 * <p>
 * This controller provides endpoints to create invitation tokens
 * that are used for registering new admin users. These tokens ensure a secure
 * and controlled process for granting admin access.
 * </p>
 *
 * <p>API Endpoints:</p>
 * <ul>
 *   <li><strong>POST /api/invitations/generate</strong> - Generates a new invitation token.</li>
 * </ul>
 *
 * @author jasmin1707
 * @author Lara
 */
@RestController
@RequestMapping("/api/invitations")
public class AdminInvitationController {

    @Autowired
    private AdminInvitationService invitationService;

    // Constructor for injecting AdminInvitationService
    public AdminInvitationController(AdminInvitationService invitationService) {
        this.invitationService = invitationService;
    }

    /**
     * Generates a new invitation token.
     * <p>
     * This endpoint is used by existing admins to generate a unique, secure invitation token
     * that can be used to register a new admin. The generated token is stored in the database
     * and returned in the response for manual sharing with the intended recipient.
     * </p>
     *
     * <p>API Details:</p>
     * <ul>
     *   <li><strong>HTTP Method:</strong> POST</li>
     *   <li><strong>URL:</strong> /api/invitations/generate</li>
     *   <li><strong>Request Body:</strong> None</li>
     *   <li><strong>Response:</strong> A string representing the generated invitation token.</li>
     * </ul>
     *
     * @return A {@link ResponseEntity} containing the generated invitation token as a string.
     *
     * @throws RuntimeException If there is an issue generating the token (e.g., database failure).
     */
    @PostMapping("/generate")
    public ResponseEntity<String> generateInvitationToken() {
        AdminInvitation invitation = invitationService.createInvitationToken();
        return ResponseEntity.ok(invitation.getToken());
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleRuntimeException(RuntimeException ex) {
        return "An error occurred: " + ex.getMessage();
    }
}
