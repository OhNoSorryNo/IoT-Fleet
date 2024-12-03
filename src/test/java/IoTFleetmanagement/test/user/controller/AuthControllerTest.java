package IoTFleetmanagement.test.user.controller;


import IoTFleetManagement.agent.model.Agent;
import IoTFleetManagement.user.controller.AuthController;
import IoTFleetManagement.common.exceptions.AlreadyExistsException;
import IoTFleetManagement.user.model.User;
import IoTFleetManagement.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;


import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private UserService userService;

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
}
