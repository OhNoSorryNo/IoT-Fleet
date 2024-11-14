package IoTFleetManagement.controller;

import IoTFleetManagement.exceptions.UsernameAlreadyExistsException;
import IoTFleetManagement.model.User;
import IoTFleetManagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller responsible for handling authentication-related requests such as login and registration.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;

    /**
     * Constructs an AuthController with the specified UserService.
     *
     * @param userService The UserService used for user authentication and registration.
     */
    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Handles user login requests.
     *
     * @param username The username of the user attempting to log in.
     * @param password The password of the user attempting to log in.
     * @return A ResponseEntity containing a success message if login is successful, or an unauthorized response if not.
     */
    @Operation(summary = "User Login", description = "Authenticates the user with the provided username and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid username or password")
    })
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String username, @RequestParam String password) {
        logger.info("Login attempt with username: [REDACTED]");

        return userService.authenticate(username, password)
                .map(user -> {
                    logger.info("Login successful for username: [REDACTED]");
                    return ResponseEntity.ok("Login successful!");
                })
                .orElseGet(() -> {
                    logger.warn("Invalid login attempt for username: [REDACTED]");
                    return new ResponseEntity<>("Invalid username or password", HttpStatus.UNAUTHORIZED);
                });
    }

    /**
     * Handles user registration requests.
     *
     * @param username The username of the user to be registered.
     * @param password The password of the user to be registered.
     * @return A ResponseEntity containing the created user if registration is successful, or an error message if the role is not found.
     */
    @Operation(summary = "User Registration", description = "Registers a new user with a specified role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User successfully registered"),
            @ApiResponse(responseCode = "400", description = "Invalid input parameters or role not found")
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestParam String email, @RequestParam String username, @RequestParam String password) {
        System.out.println("Register endpoint hit with username: " + username);
        String roleName = "ROLE_USER";
        // Check if the role exists
        if (!userService.roleExists(roleName)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Role not found: " + roleName);
        }

        try {
            // Attempt to register the new user
            User newUser = userService.registerUser(email, username, password, roleName);
            logger.info("User registered successfully with username: [REDACTED] and role: {}", roleName);
            return ResponseEntity.status(HttpStatus.CREATED).body(newUser);

        } catch (UsernameAlreadyExistsException e) {
            // Handle the exception if the username is already taken
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username '" + username + "' is already taken.");
        }
    }

}
