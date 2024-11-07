package IoTFleetmanagement.test;

import IoTFleetManagement.exceptions.UsernameAlreadyExistsException;
import IoTFleetManagement.model.Role;
import IoTFleetManagement.model.User;
import IoTFleetManagement.repository.RoleRepository;
import IoTFleetManagement.repository.UserRepository;
import IoTFleetManagement.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

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
        User user = new User(username, "password", new Role("ROLE_USER"));
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
        User user = new User("testUser", "password", new Role("ROLE_USER"));
        when(userRepository.save(user)).thenReturn(user);

        // Act
        User savedUser = userService.saveUser(user);

        // Assert
        assertNotNull(savedUser);
        assertEquals("testUser", savedUser.getUsername());
    }

    @Test
    void testRegisterUserSuccess() {
        // Arrange
        String username = "newUser";
        String password = "password";
        String roleName = "ROLE_USER";
        Role role = new Role(roleName);
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(roleRepository.findByName(roleName)).thenReturn(role);
        when(userRepository.save(new User(username, password, role))).thenReturn(new User(username, password, role));

        // Act
        User registeredUser = userService.registerUser(username, password, roleName);

        // Assert
        assertNotNull(registeredUser);
        assertEquals(username, registeredUser.getUsername());
        assertEquals(roleName, registeredUser.getRole().getName());
    }

    @Test
    void testRegisterUserUsernameAlreadyExists() {
        // Arrange
        String username = "existingUser";
        String password = "password";
        String roleName = "ROLE_USER";
        when(userRepository.existsByUsername(username)).thenReturn(true);

        // Act & Assert
        assertThrows(UsernameAlreadyExistsException.class, () -> userService.registerUser(username, password, roleName));
    }

    @Test
    void testAuthenticateSuccess() {
        // Arrange
        String username = "testUser";
        String password = "password";
        User user = new User(username, password, new Role("ROLE_USER"));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Act
        Optional<User> authenticatedUser = userService.authenticate(username, password);

        // Assert
        assertTrue(authenticatedUser.isPresent());
        assertEquals(username, authenticatedUser.get().getUsername());
    }

    @Test
    void testAuthenticateFailure() {
        // Arrange
        String username = "testUser";
        String password = "wrongPassword";
        User user = new User(username, "password", new Role("ROLE_USER"));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Act
        Optional<User> authenticatedUser = userService.authenticate(username, password);

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
}
