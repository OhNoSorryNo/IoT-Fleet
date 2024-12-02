
package IoTFleetManagement.user.service;

import IoTFleetManagement.agent.model.Agent;
import IoTFleetManagement.agent.repository.AgentRepository;
import IoTFleetManagement.common.exceptions.AlreadyExistsException;
import IoTFleetManagement.user.model.Role;
import IoTFleetManagement.user.model.User;
import IoTFleetManagement.user.repository.RoleRepository;
import IoTFleetManagement.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for managing users in the IoT Fleet Management system.
 * <p>
 * This class provides business logic for user-related operations such as
 * authentication, registration, and association with roles and agents.
 *
 * @author Lara
 * @author Jasmin1707
 * @author nico
 */
@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AgentRepository agentRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructs a {@code UserService} with the necessary dependencies.
     *
     * @param userRepository  the repository for user data
     * @param roleRepository  the repository for role data
     * @param passwordEncoder the encoder for securing user passwords
     * @param agentRepository the repository for agent data
     */
    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, AgentRepository agentRepository) {
        this.userRepository = userRepository;
        this.agentRepository = agentRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Finds a user by their username.
     *
     * @param username the username to search for
     * @return an {@link Optional} containing the user if found, or empty if not
     */
    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Saves a user to the database after encoding their password.
     *
     * @param user the user to save
     * @return the saved user
     */
    public User saveUser(User user) {
        // Hashing the password before saving the user
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    /**
     * Registers a new user with a given email, username, and password.
     *
     * @param email    the email address of the user
     * @param username the username of the user
     * @param password the raw password of the user
     * @return the registered user
     * @throws AlreadyExistsException if the username or email is already in use
     */
    public User registerUser(String email, String username, String password) {
        String roleName = "ROLE_USER";
        if (userRepository. findByUsername(username).isPresent()) {
            throw new AlreadyExistsException("Username '" + username + "' is already taken.");
            // Check if email already exists
        } else if (userRepository.findByEmail(email).isPresent()) {
            throw new AlreadyExistsException("Email '" + email + "' is already taken.");
        }

        Role role = roleRepository.findByName(roleName);
        // Hash the Password
        String hashedPassword = passwordEncoder.encode(password);
        User user = new User(email, username, hashedPassword, role);
        return userRepository.save(user);
    }

    /**
     * Authenticates a user by their username and raw password.
     *
     * @param username    the username of the user
     * @param rawPassword the raw (unhashed) password of the user
     * @return an {@link Optional} containing the user if authentication succeeds, or empty if it fails
     */
    public Optional<User> authenticate(String username, String rawPassword) {
        return userRepository.findByUsername(username)
                .filter(user -> passwordEncoder.matches(rawPassword, user.getPassword()));
    }

    /**
     * Checks if a role exists in the system.
     *
     * @param roleName the name of the role to check
     * @return {@code true} if the role exists, {@code false} otherwise
     */
    public boolean roleExists(String roleName) {
        return roleRepository.findByName(roleName) != null;
    }

    /**
     * Retrieves a list of agents associated with a specific user.
     *
     * @param userId the ID of the user
     * @return a list of agents linked to the user
     */
    public List<Agent> getAgentsByUser(Long userId) {
        return agentRepository.findByUserId(userId);
    }
}
