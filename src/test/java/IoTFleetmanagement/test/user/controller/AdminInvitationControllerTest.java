package IoTFleetmanagement.test.user.controller;

import IoTFleetManagement.user.controller.AdminInvitationController;
import IoTFleetManagement.user.model.AdminInvitation;
import IoTFleetManagement.user.repository.UserRepository;
import IoTFleetManagement.user.service.AdminInvitationService;
import IoTFleetManagement.user.service.CustomUserDetailsService;
import IoTFleetmanagement.test.security.config.TestSecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for {@link AdminInvitationController}.
 */
@WebMvcTest(AdminInvitationController.class)
@Import(TestSecurityConfig.class) // Import TestSecurityConfig to disable CSRF and simplify security
@ContextConfiguration(classes = {AdminInvitationController.class})
public class AdminInvitationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminInvitationService adminInvitationService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private AdminInvitation testInvitation;

    @BeforeEach
    void setUp() {
        testInvitation = new AdminInvitation();
        testInvitation.setId(1L);
        testInvitation.setToken("sample-invitation-token");
    }

    /**
     * Test that the generateInvitationToken endpoint returns a 200 status with the correct token.
     */
    @Test
    @WithMockUser(roles = "ADMIN") // Simulate an authenticated admin user
    public void testGenerateInvitationToken_Success() throws Exception {
        // Mock the service call to return the test invitation
        when(adminInvitationService.createInvitationToken()).thenReturn(testInvitation);

        // Perform the request and validate the response
        mockMvc.perform(post("/api/invitations/generate")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Expect HTTP 200 OK
                .andExpect(content().string("sample-invitation-token")); // Expect the correct token
    }

    /**
     * Test that the generateInvitationToken endpoint handles a service error properly.
     */
    @Test
    @WithMockUser(roles = "ADMIN") // Simulate an authenticated admin user
    public void testGenerateInvitationToken_ServiceError() throws Exception {
        // Mock the service call to throw an exception
        when(adminInvitationService.createInvitationToken())
                .thenThrow(new RuntimeException("Database error"));

        // Perform the request and validate the response
        mockMvc.perform(post("/api/invitations/generate")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError()); // Expect HTTP 500 Server Error
    }
}