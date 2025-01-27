package IoTFleetManagment.test.user.controller;

import IoTFleetManagement.agent.model.Agent;
import IoTFleetManagement.user.controller.AuthController;
import IoTFleetManagement.common.exceptions.AlreadyExistsException;
import IoTFleetManagement.user.model.AdminInvitation;
import IoTFleetManagement.user.model.User;
import IoTFleetManagement.user.service.AdminInvitationService;
import IoTFleetManagement.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;


import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private AdminInvitationService adminInvitationService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void loginSuccess() {
        // Arrange
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpSession mockSession = mock(HttpSession.class);
        String username = "testUser";
        String password = "testPassword";

        User mockUser = new User();
        when(userService.authenticate(username, password)).thenReturn(Optional.of(mockUser));
        when(mockRequest.getSession(true)).thenReturn(mockSession);

        // Act
        ResponseEntity<String> response = authController.login(mockRequest, username, password);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Login successful!", response.getBody());
        verify(mockSession).setAttribute(eq(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY), any());
    }

    @Test
    void loginFailure() {
        // Arrange
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        String username = "invalidUser";
        String password = "wrongPassword";

        when(userService.authenticate(username, password)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<String> response = authController.login(mockRequest, username, password);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid username or password", response.getBody());
    }

    @Test
    void registerSuccess() throws AlreadyExistsException {
        // Arrange
        String email = "newUser@gmail.com";
        String username = "newUser";
        String password = "password";
        String roleName = "ROLE_USER";
        User mockUser = new User();

        when(userService.roleExists(roleName)).thenReturn(true);
        when(userService.registerUser(email, username, password)).thenReturn(mockUser);

        // Act
        ResponseEntity<?> response = authController.register(email, username, password);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(mockUser, response.getBody());
    }

    @Test
    void registerRoleNotFound() {
        // Arrange
        String email = "newUser@gmail.com";
        String username = "newUser";
        String password = "password";
        String roleName = "ROLE_USER";

        when(userService.roleExists(roleName)).thenReturn(false);

        // Act
        ResponseEntity<?> response = authController.register(email, username, password);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Role not found: " + roleName, response.getBody());
    }

    @Test
    void registerUsernameAlreadyTaken() throws AlreadyExistsException {
        // Arrange
        String email = "newUser@gmail.com";
        String username = "existingUser";
        String password = "password";
        String roleName = "ROLE_USER";

        when(userService.roleExists(roleName)).thenReturn(true);
        when(userService.registerUser(email, username, password)).thenThrow(new AlreadyExistsException("Username already exists"));

        // Act
        ResponseEntity<?> response = authController.register(email, username, password);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Username '" + username + "' is already taken.", response.getBody());
    }

    @Test
    void getUserAgentsSuccess() {
        // Arrange
        Long userId = 1L;
        Agent mockAgent = new Agent();
        List<Agent> mockAgents = Collections.singletonList(mockAgent);

        when(userService.getAgentsByUser(userId)).thenReturn(mockAgents);

        // Act
        ResponseEntity<List<Agent>> response = authController.getUserAgents(userId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockAgents, response.getBody());
    }

    @Test
    void registerAdminSuccess() throws AlreadyExistsException {
        // Arrange
        String email = "admin@example.com";
        String username = "adminUser";
        String password = "securePassword";
        String invitationToken = "validToken";

        AdminInvitation mockInvitation = new AdminInvitation();
        User mockAdmin = new User();

        when(adminInvitationService.validateToken(invitationToken)).thenReturn(Optional.of(mockInvitation));
        when(userService.registerAdmin(email, username, password)).thenReturn(mockAdmin);

        // Act
        ResponseEntity<?> response = authController.registerAdmin(email, username, password, invitationToken);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(mockAdmin, response.getBody());
        verify(adminInvitationService, times(1)).markTokenAsUsed(mockInvitation);
    }

    @Test
    void registerAdminInvalidToken() {
        // Arrange
        String email = "admin@example.com";
        String username = "adminUser";
        String password = "securePassword";
        String invitationToken = "invalidToken";

        when(adminInvitationService.validateToken(invitationToken)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = authController.registerAdmin(email, username, password, invitationToken);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid or expired invitation token.", response.getBody());
        verify(adminInvitationService, never()).markTokenAsUsed(any());
    }

    @Test
    void registerAdminAlreadyExists() throws AlreadyExistsException {
        // Arrange
        String email = "admin@example.com";
        String username = "existingAdmin";
        String password = "securePassword";
        String invitationToken = "validToken";

        AdminInvitation mockInvitation = new AdminInvitation();

        when(adminInvitationService.validateToken(invitationToken)).thenReturn(Optional.of(mockInvitation));
        when(userService.registerAdmin(email, username, password)).thenThrow(new AlreadyExistsException("Admin already exists"));

        // Act
        ResponseEntity<?> response = authController.registerAdmin(email, username, password, invitationToken);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Admin already exists", response.getBody());
        verify(adminInvitationService, never()).markTokenAsUsed(mockInvitation);
    }

    @Test
    void checkAuthenticationSuccess() {
        // Arrange
        Authentication mockAuthentication = mock(Authentication.class);
        SecurityContext mockSecurityContext = mock(SecurityContext.class);

        SecurityContextHolder.setContext(mockSecurityContext);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        when(mockAuthentication.isAuthenticated()).thenReturn(true);
        when(mockAuthentication.getPrincipal()).thenReturn(new User()); // Replace with your user object

        // Act
        ResponseEntity<?> response = authController.checkAuthentication();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void checkAuthenticationUnauthenticated() {
        // Arrange
        SecurityContext mockSecurityContext = mock(SecurityContext.class);

        SecurityContextHolder.setContext(mockSecurityContext);
        when(mockSecurityContext.getAuthentication()).thenReturn(null);

        // Act
        ResponseEntity<?> response = authController.checkAuthentication();

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Not authenticated", response.getBody());
    }

    @Test
    void checkAuthenticationAnonymousUser() {
        // Arrange
        Authentication mockAuthentication = mock(Authentication.class);
        SecurityContext mockSecurityContext = mock(SecurityContext.class);

        SecurityContextHolder.setContext(mockSecurityContext);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        when(mockAuthentication.isAuthenticated()).thenReturn(false);
        when(mockAuthentication.getPrincipal()).thenReturn("anonymousUser");

        // Act
        ResponseEntity<?> response = authController.checkAuthentication();

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Not authenticated", response.getBody());
    }

    @Test
    void logoutAuthenticatedUser() {
        // Arrange
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        Authentication mockAuthentication = mock(Authentication.class);
        SecurityContext mockSecurityContext = mock(SecurityContext.class);

        SecurityContextHolder.setContext(mockSecurityContext);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);

        SecurityContextLogoutHandler mockLogoutHandler = mock(SecurityContextLogoutHandler.class);

        // Act
        ResponseEntity<?> response = authController.logout(mockRequest, mockResponse);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Logout successful", response.getBody());
        verify(mockSecurityContext, times(1)).getAuthentication();
    }

    @Test
    void logoutUnauthenticatedUser() {
        // Arrange
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        SecurityContext mockSecurityContext = mock(SecurityContext.class);

        SecurityContextHolder.setContext(mockSecurityContext);
        when(mockSecurityContext.getAuthentication()).thenReturn(null);

        // Act
        ResponseEntity<?> response = authController.logout(mockRequest, mockResponse);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Logout successful", response.getBody());
        verify(mockSecurityContext, times(1)).getAuthentication();
    }

    @Test
    void getUserAgentsAuthenticated() {
        // Arrange
        Authentication mockAuthentication = mock(Authentication.class);
        SecurityContext mockSecurityContext = mock(SecurityContext.class);
        User mockUser = mock(User.class);
        List<Agent> mockAgents = List.of(new Agent(), new Agent());

        SecurityContextHolder.setContext(mockSecurityContext);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        when(mockAuthentication.isAuthenticated()).thenReturn(true);
        when(mockAuthentication.getPrincipal()).thenReturn(mockUser);
        when(mockUser.getId()).thenReturn(1L);
        when(userService.getAgentsByUser(1L)).thenReturn(mockAgents);

        // Act
        ResponseEntity<List<Agent>> response = authController.getUserAgents();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockAgents, response.getBody());
        verify(userService, times(1)).getAgentsByUser(1L);
    }

    @Test
    void getUserAgentsUnauthenticated() {
        // Arrange
        Authentication mockAuthentication = mock(Authentication.class);
        SecurityContext mockSecurityContext = mock(SecurityContext.class);

        SecurityContextHolder.setContext(mockSecurityContext);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        when(mockAuthentication.isAuthenticated()).thenReturn(false);

        // Act
        ResponseEntity<List<Agent>> response = authController.getUserAgents();

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
        verify(userService, never()).getAgentsByUser(anyLong());
    }

    @Test
    void getUserAgentsNoAuthentication() {
        // Arrange
        SecurityContext mockSecurityContext = mock(SecurityContext.class);

        SecurityContextHolder.setContext(mockSecurityContext);
        when(mockSecurityContext.getAuthentication()).thenReturn(null);

        // Act
        ResponseEntity<List<Agent>> response = authController.getUserAgents();

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
        verify(userService, never()).getAgentsByUser(anyLong());
    }

    @Test
    void updateUiNameAuthenticatedSuccess() {
        // Arrange
        String newUiName = "NewUIName";
        Authentication mockAuthentication = mock(Authentication.class);
        SecurityContext mockSecurityContext = mock(SecurityContext.class);
        User mockUser = mock(User.class);
        User updatedUser = mock(User.class);

        SecurityContextHolder.setContext(mockSecurityContext);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        when(mockAuthentication.isAuthenticated()).thenReturn(true);
        when(mockAuthentication.getPrincipal()).thenReturn(mockUser);
        when(mockUser.getId()).thenReturn(1L);
        when(userService.updateUiName(1L, newUiName)).thenReturn(updatedUser);

        // Act
        ResponseEntity<?> response = authController.updateUiName(newUiName);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedUser, response.getBody());
        verify(userService, times(1)).updateUiName(1L, newUiName);
    }

    @Test
    void updateUiNameUnauthenticated() {
        // Arrange
        Authentication mockAuthentication = mock(Authentication.class);
        SecurityContext mockSecurityContext = mock(SecurityContext.class);

        SecurityContextHolder.setContext(mockSecurityContext);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        when(mockAuthentication.isAuthenticated()).thenReturn(false);

        // Act
        ResponseEntity<?> response = authController.updateUiName("NewUIName");

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Not authenticated", response.getBody());
        verify(userService, never()).updateUiName(anyLong(), anyString());
    }

    @Test
    void updateUiNameNoAuthentication() {
        // Arrange
        SecurityContext mockSecurityContext = mock(SecurityContext.class);

        SecurityContextHolder.setContext(mockSecurityContext);
        when(mockSecurityContext.getAuthentication()).thenReturn(null);

        // Act
        ResponseEntity<?> response = authController.updateUiName("NewUIName");

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Not authenticated", response.getBody());
        verify(userService, never()).updateUiName(anyLong(), anyString());
    }

    @Test
    void updateUiNameInvalidInput() {
        // Arrange
        String invalidUiName = ""; // Invalid UI name
        Authentication mockAuthentication = mock(Authentication.class);
        SecurityContext mockSecurityContext = mock(SecurityContext.class);
        User mockUser = mock(User.class);

        SecurityContextHolder.setContext(mockSecurityContext);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        when(mockAuthentication.isAuthenticated()).thenReturn(true);
        when(mockAuthentication.getPrincipal()).thenReturn(mockUser);
        when(mockUser.getId()).thenReturn(1L);
        when(userService.updateUiName(1L, invalidUiName)).thenThrow(new IllegalArgumentException("Invalid UI name"));

        // Act
        ResponseEntity<?> response = authController.updateUiName(invalidUiName);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid UI name", response.getBody());
        verify(userService, times(1)).updateUiName(1L, invalidUiName);
    }
}


