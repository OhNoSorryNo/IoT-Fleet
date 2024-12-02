package IoTFleetManagement.user.controller;

import IoTFleetManagement.common.exceptions.AlreadyExistsException;
import IoTFleetManagement.user.model.User;
import IoTFleetManagement.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import IoTFleetManagement.agent.model.Agent;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.Collections;
import java.util.List;

/**
 * Controller responsible for handling user authentication and registration requests.
 * <p>
 * Provides REST endpoints for user login, registration, and retrieving user-specific agents.
 *
 * @author Lara
 * @author Jasmin1707
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
     * Authenticates a user based on provided credentials.
     * <p>
     * On successful authentication, it initializes a security context and associates it with the current session.
     *
     * @param request  the HTTP request object
     * @param username the username of the user attempting to log in
     * @param password the password of the user attempting to log in
     * @return a {@link ResponseEntity} containing a success message if authentication is successful,
     * or an unauthorized response if credentials are invalid
     */
    @Operation(summary = "User Login", description = "Authenticates the user with the provided username and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid username or password")
    })
    @PostMapping("/login")
    public ResponseEntity<String> login(HttpServletRequest request, @RequestParam String username, @RequestParam String password) {
        logger.info("Login attempt with username: " + username);

        return userService.authenticate(username, password)
                .map(user -> {
                    // Log successful login
                    logger.info("Login attempt");

                    // Create or get the session
                    HttpSession session = request.getSession(true); // Create a session if it doesn't exist

                    //new
                    // Update the SecurityContext with the authenticated user
                    Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    context.setAuthentication(authentication);

                    // Save the SecurityContext in the session
                    session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
                    SecurityContextHolder.setContext(context);

                    return ResponseEntity.ok("Login successful!");
                })
                .orElseGet(() -> {
                    // Log failed login attempt
                    logger.warn("Invalid login attempt for username: [REDACTED]");
                    return new ResponseEntity<>("Invalid username or password", HttpStatus.UNAUTHORIZED);
                });
    }

    /**
     * Registers a new user with the default "ROLE_USER" role.
     * <p>
     * Validates if the role exists and checks for unique username and email before creating a new user.
     *
     * @param email    the email of the user to be registered
     * @param username the username of the user to be registered
     * @param password the password of the user to be registered
     * @return a {@link ResponseEntity} containing the newly created user if registration is successful,
     * or an error message if the username, email, or role is invalid
     */
    @Operation(summary = "User Registration", description = "Registers a new user with a specified role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User successfully registered"),
            @ApiResponse(responseCode = "400", description = "Invalid input parameters or role not found")
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestParam("email") String email,
            @RequestParam("username") String username,
            @RequestParam("password") String password) {
        System.out.println("Register endpoint hit with username: " + username);
        //Default user role.
        String roleName = "ROLE_USER";
        // Check if the role exists
        if (!userService.roleExists(roleName)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Role not found: " + roleName);
        }

        try {
            // Attempt to register the new user
            User newUser = userService.registerUser(email, username, password);
            logger.info("User registered successfully with username: [REDACTED] and role: {}", roleName);
            return ResponseEntity.status(HttpStatus.CREATED).body(newUser);

        } catch (AlreadyExistsException e) {
            // Handle the exception if the username is already taken
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username '" + username + "' is already taken.");
        }
    }

    /**
     * Retrieves a list of agents associated with a specific user.
     *
     * @param userId the unique ID of the user whose agents are to be retrieved
     * @return a {@link ResponseEntity} containing a list of {@link Agent} objects associated with the user
     */
    @GetMapping("/{userId}/agents")
    public ResponseEntity<List<Agent>> getUserAgents(@PathVariable Long userId) {
        List<Agent> agents = userService.getAgentsByUser(userId);
        return ResponseEntity.ok(agents);
    }

}
