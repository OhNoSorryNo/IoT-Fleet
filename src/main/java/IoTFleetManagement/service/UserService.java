package IoTFleetManagement.service;

import IoTFleetManagement.model.Role;
import IoTFleetManagement.model.User;
import IoTFleetManagement.repository.RoleRepository;
import IoTFleetManagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service class for managing User-related operations.
 * <p>
 * This class provides methods for user registration, authentication, and other user-related functionalities.
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    /**
     * Constructor for UserService.
     *
     * @param userRepository the repository to manage User entities
     * @param roleRepository the repository to manage Role entities
     */
    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    /**
     * Find a user by their username.
     *
     * @param username the username of the user to find
     * @return an Optional containing the User entity if found, or empty if not found
     */
    public Optional<User> findUserByUsername(String username) {
        logger.debug("Attempting to find user by username: {}", username);
        return userRepository.findByUsername(username);
    }

    /**
     * Save a user to the database.
     *
     * @param user the User entity to save
     * @return the saved User entity
     */
    public User saveUser(User user) {
        logger.debug("Saving user: {}", user.getUsername());
        return userRepository.save(user);
    }

    /**
     * Register a new user with a given username, password, and role.
     *
     * @param username the username of the new user
     * @param password the password of the new user
     * @param roleName the name of the role to assign to the user
     * @return the newly registered User entity
     */
    public User registerUser(String username, String password, String roleName) {
        logger.debug("Registering user with username: {} and role: {}", username, roleName);
        Role role = roleRepository.findByName(roleName); // Find the role by name
        if (role == null) {
            logger.warn("Role not found: {}", roleName);
            throw new IllegalArgumentException("Role not found: " + roleName);
        }
        User user = new User(username, password, role);
        return userRepository.save(user);
    }

    /**
     * Authenticate a user based on their username and password.
     *
     * @param username the username of the user
     * @param password the password of the user
     * @return an Optional containing the authenticated User entity if credentials match, or empty if not
     */
    public Optional<User> authenticate(String username, String password) {
        logger.debug("Authenticating user with username: {}", username);
        return userRepository.findByUsername(username)
                .filter(user -> {
                    boolean passwordMatches = user.getPassword().equals(password);
                    if (!passwordMatches) {
                        logger.warn("Authentication failed for username: {}", username);
                    }
                    return passwordMatches;
                }); // Simple password check
    }

    /**
     * Check if a role with the given name exists.
     *
     * @param roleName the name of the role to check
     * @return true if the role exists, false otherwise
     */
    public boolean roleExists(String roleName) {
        logger.debug("Checking if role exists: {}", roleName);
        return roleRepository.findByName(roleName) != null;
    }

}
