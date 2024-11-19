package IoTFleetmanagement.test.user.controller;


import IoTFleetManagement.user.controller.AuthController;
import IoTFleetManagement.common.exceptions.AlreadyExistsException;
import IoTFleetManagement.user.model.User;
import IoTFleetManagement.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

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
        // Arrange
        String username = "testUser";
        String password = "testPassword";
        User mockUser = new User();
        when(userService.authenticate(username, password)).thenReturn(Optional.of(mockUser));

        // Act
        ResponseEntity<String> response = authController.login(username, password);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Login successful!", response.getBody());
    }

    @Test
    void loginFailure() {
        // Arrange
        String username = "testUser";
        String password = "wrongPassword";
        when(userService.authenticate(username, password)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<String> response = authController.login(username, password);

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
