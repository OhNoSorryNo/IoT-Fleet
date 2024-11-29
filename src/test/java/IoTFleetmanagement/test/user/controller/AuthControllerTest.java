package IoTFleetmanagement.test.user.controller;


import IoTFleetManagement.user.controller.AuthController;
import IoTFleetManagement.common.exceptions.AlreadyExistsException;
import IoTFleetManagement.user.model.User;
import IoTFleetManagement.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

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
        // Mock HttpServletRequest
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);

        // Mock username and password
        String username = "test";
        String password = "test1234";

        // Call the login method
        ResponseEntity<String> response = authController.login(mockRequest, username, password);

        // Verify the response
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Login successful!", response.getBody());
    }

    @Test
    void loginFailure() {
        // Mock HttpServletRequest
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);

        // Mock invalid username and password
        String username = "invalid";
        String password = "wrongPassword";

        // Call the login method
        ResponseEntity<String> response = authController.login(mockRequest, username, password);

        // Verify the response
        assertEquals(401, response.getStatusCodeValue());
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
        ResponseEntity<?> response = authController.register(email,username, password);

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
}
