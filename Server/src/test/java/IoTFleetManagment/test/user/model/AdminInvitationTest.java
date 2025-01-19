package IoTFleetManagment.test.user.model;

import IoTFleetManagement.user.model.AdminInvitation;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AdminInvitationTest {

    @Test
    void testDefaultValues() {
        AdminInvitation invitation = new AdminInvitation();

        assertNull(invitation.getId(), "ID should be null by default");
        assertNull(invitation.getToken(), "Token should be null by default");
        assertFalse(invitation.isUsed(), "Used should default to false");
        assertNotNull(invitation.getCreatedAt(), "CreatedAt should not be null by default");
        assertNull(invitation.getExpiresAt(), "ExpiresAt should be null by default");
        assertNull(invitation.getUsedAt(), "UsedAt should be null by default");
    }

    @Test
    void testSetAndGetId() {
        AdminInvitation invitation = new AdminInvitation();
        Long id = 123L;

        invitation.setId(id);
        assertEquals(id, invitation.getId(), "ID should be set and retrieved correctly");
    }

    @Test
    void testSetAndGetToken() {
        AdminInvitation invitation = new AdminInvitation();
        String token = "sample-token";

        invitation.setToken(token);
        assertEquals(token, invitation.getToken(), "Token should be set and retrieved correctly");
    }

    @Test
    void testSetAndGetUsed() {
        AdminInvitation invitation = new AdminInvitation();

        invitation.setUsed(true);
        assertTrue(invitation.isUsed(), "Used should be set to true");

        invitation.setUsed(false);
        assertFalse(invitation.isUsed(), "Used should be set to false");
    }

    @Test
    void testSetAndGetCreatedAt() {
        AdminInvitation invitation = new AdminInvitation();
        LocalDateTime now = LocalDateTime.now();

        invitation.setCreatedAt(now);
        assertEquals(now, invitation.getCreatedAt(), "CreatedAt should be set and retrieved correctly");
    }

    @Test
    void testSetAndGetExpiresAt() {
        AdminInvitation invitation = new AdminInvitation();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(1);

        invitation.setExpiresAt(expiresAt);
        assertEquals(expiresAt, invitation.getExpiresAt(), "ExpiresAt should be set and retrieved correctly");
    }

    @Test
    void testSetAndGetUsedAt() {
        AdminInvitation invitation = new AdminInvitation();
        LocalDateTime usedAt = LocalDateTime.now();

        invitation.setUsedAt(usedAt);
        assertEquals(usedAt, invitation.getUsedAt(), "UsedAt should be set and retrieved correctly");
    }
}


