package IoTFleetManagment.test.user.service;

import IoTFleetManagement.agent.model.Agent;
import IoTFleetManagement.agent.repository.AgentRepository;
import IoTFleetManagement.common.exceptions.AlreadyExistsException;
import IoTFleetManagement.user.model.Role;
import IoTFleetManagement.user.model.User;
import IoTFleetManagement.user.repository.RoleRepository;
import IoTFleetManagement.user.repository.UserRepository;
import IoTFleetManagement.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindUserByUsername() {
        // Arrange
        String username = "testUser";
        String email = "test@example.com";
        User user = new User(email, username, "password", new Role("ROLE_USER"));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Act
        Optional<User> foundUser = userService.findUserByUsername(username);

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals(username, foundUser.get().getUsername());
    }

    @Test
    void testSaveUser() {
        // Arrange
        User user = new User("test@example.com", "testUser", "password", new Role("ROLE_USER"));
        when(passwordEncoder.encode("password")).thenReturn("hashedPassword");
        when(userRepository.save(user)).thenReturn(user);

        // Act
        User savedUser = userService.saveUser(user);

        // Assert
        assertNotNull(savedUser);
        assertEquals("testUser", savedUser.getUsername());
        assertEquals("hashedPassword", savedUser.getPassword());
    }

    @Test
    void testRegisterUserSuccess() {
        // Arrange
        String email = "test@example.com";
        String username = "newUser";
        String rawPassword = "password";
        String encodedPassword = "encodedPassword";
        String roleName = "ROLE_USER";
        Role role = new Role(roleName);

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(roleRepository.findByName(roleName)).thenReturn(role);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        // Use ArgumentCaptor to capture the User object passed to save
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User registeredUser = userService.registerUser(email, username, rawPassword);

        // Assert
        assertNotNull(registeredUser, "Registered user should not be null");
        assertEquals(username, registeredUser.getUsername());
        assertEquals(roleName, registeredUser.getRole().getName());
        assertEquals(encodedPassword, registeredUser.getPassword());

        // Verify that userRepository.save(...) was called
        verify(userRepository).save(any(User.class));

        // Optionally, assert that the captured User has expected properties
        User savedUser = userCaptor.getValue();
        assertNotNull(savedUser);
        assertEquals(username, savedUser.getUsername());
        assertEquals(encodedPassword, savedUser.getPassword());
        assertEquals(role, savedUser.getRole());
    }


    @Test
    void testRegisterUserUsernameAlreadyExists() {
        // Arrange
        String email = "test@example.com";
        String username = "existingUser";
        String password = "password";

        // Mock the repository to indicate the username already exists
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(new User()));

        // Act & Assert
        assertThrows(AlreadyExistsException.class, () -> userService.registerUser(email, username, password));
    }

    @Test
    void testRegisterUserEmailAlreadyExists() {
        // Arrange
        String email = "test@example.com";
        String username = "newUser";
        String password = "password";

        // Mock the repository to indicate the email already exists
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User()));

        // Act & Assert
        assertThrows(AlreadyExistsException.class, () -> userService.registerUser(email, username, password));
    }


    @Test
    void testAuthenticateSuccess() {
        // Arrange
        String email = "test@example.com";
        String username = "testUser";
        String rawPassword = "password";
        String encodedPassword = "encodedPassword";

        User user = new User(email, username, encodedPassword, new Role("ROLE_USER"));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Mock the passwordEncoder.matches(...) method
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        // Act
        Optional<User> authenticatedUser = userService.authenticate(username, rawPassword);

        // Assert
        assertTrue(authenticatedUser.isPresent());
        assertEquals(username, authenticatedUser.get().getUsername());
    }

    @Test
    void testAuthenticateFailure() {
        // Arrange
        String email = "test@example.com";
        String username = "testUser";
        String rawPassword = "wrongPassword";
        String encodedPassword = "encodedPassword";

        User user = new User(email, username, encodedPassword, new Role("ROLE_USER"));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Mock the passwordEncoder.matches(...) method
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        // Act
        Optional<User> authenticatedUser = userService.authenticate(username, rawPassword);

        // Assert
        assertFalse(authenticatedUser.isPresent());
    }


    @Test
    void testRoleExists() {
        // Arrange
        String roleName = "ROLE_USER";
        when(roleRepository.findByName(roleName)).thenReturn(new Role(roleName));

        // Act
        boolean roleExists = userService.roleExists(roleName);

        // Assert
        assertTrue(roleExists);
    }

    @Test
    void testRoleDoesNotExist() {
        // Arrange
        String roleName = "ROLE_NON_EXISTENT";
        when(roleRepository.findByName(roleName)).thenReturn(null);

        // Act
        boolean roleExists = userService.roleExists(roleName);

        // Assert
        assertFalse(roleExists);
    }

    @Test
    void testGetAgentsByUser() {
        // Arrange
        Long userId = 1L;
        Agent agent1 = new Agent();
        Agent agent2 = new Agent();
        when(agentRepository.findByUserId(userId)).thenReturn(List.of(agent1, agent2));

        // Act
        List<Agent> agents = userService.getAgentsByUser(userId);

        // Assert
        assertNotNull(agents, "Agents list should not be null");
        assertEquals(2, agents.size(), "Agents list size should match");
    }

    @Test
    void testRegisterAdminSuccess() {
        // Arrange
        String email = "admin@example.com";
        String username = "adminUser";
        String password = "securePassword";
        String encodedPassword = "hashedPassword";
        String roleName = "ROLE_ADMIN";
        Role adminRole = new Role(roleName);

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(roleRepository.findByName(roleName)).thenReturn(adminRole);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);

        // Use ArgumentCaptor to capture the User object passed to save
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User registeredAdmin = userService.registerAdmin(email, username, password);

        // Assert
        assertNotNull(registeredAdmin, "Registered admin should not be null");
        assertEquals(username, registeredAdmin.getUsername(), "Username should match");
        assertEquals(email, registeredAdmin.getEmail(), "Email should match");
        assertEquals(encodedPassword, registeredAdmin.getPassword(), "Password should be hashed");
        assertEquals(adminRole, registeredAdmin.getRole(), "Role should be ROLE_ADMIN");

        // Verify that userRepository.save(...) was called
        verify(userRepository).save(any(User.class));

        // Optionally, assert the captured User
        User savedAdmin = userCaptor.getValue();
        assertNotNull(savedAdmin);
        assertEquals(username, savedAdmin.getUsername());
        assertEquals(email, savedAdmin.getEmail());
        assertEquals(encodedPassword, savedAdmin.getPassword());
        assertEquals(adminRole, savedAdmin.getRole());
    }

    @Test
    void testRegisterAdminUsernameAlreadyExists() {
        // Arrange
        String email = "admin@example.com";
        String username = "existingUser";
        String password = "securePassword";

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(new User()));

        // Act & Assert
        AlreadyExistsException exception = assertThrows(AlreadyExistsException.class, () ->
                userService.registerAdmin(email, username, password));
        assertEquals("Username '" + username + "' is already taken.", exception.getMessage());
    }

    @Test
    void testRegisterAdminEmailAlreadyExists() {
        // Arrange
        String email = "existing@example.com";
        String username = "adminUser";
        String password = "securePassword";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User()));

        // Act & Assert
        AlreadyExistsException exception = assertThrows(AlreadyExistsException.class, () ->
                userService.registerAdmin(email, username, password));
        assertEquals("Email '" + email + "' is already taken.", exception.getMessage());
    }

    @Test
    void testRegisterAdminRoleNotFound() {
        // Arrange
        String email = "admin@example.com";
        String username = "adminUser";
        String password = "securePassword";
        String roleName = "ROLE_ADMIN";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(roleRepository.findByName(roleName)).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                userService.registerAdmin(email, username, password));
        assertEquals("Role '" + roleName + "' not found.", exception.getMessage());
    }

    @Test
    void testUpdateUiNameSuccess() {
        // Arrange
        Long userId = 1L;
        String newUiName = "New UI Name";
        User user = new User("test@example.com", "testUser", "password", new Role("ROLE_USER"));
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        // Act
        User updatedUser = userService.updateUiName(userId, newUiName);

        // Assert
        assertNotNull(updatedUser, "Updated user should not be null");
        assertEquals(newUiName, updatedUser.getUiName(), "UI Name should be updated");
        verify(userRepository).save(user); // Ensure save was called
    }

    @Test
    void testUpdateUiNameUserNotFound() {
        // Arrange
        Long userId = 1L;
        String newUiName = "New UI Name";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                userService.updateUiName(userId, newUiName));
        assertEquals("User not found with ID: " + userId, exception.getMessage());
    }

}


