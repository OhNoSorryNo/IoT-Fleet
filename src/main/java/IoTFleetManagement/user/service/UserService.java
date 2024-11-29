
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

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AgentRepository agentRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, AgentRepository agentRepository) {
        this.userRepository = userRepository;
        this.agentRepository = agentRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }


    public User saveUser(User user) {
        // Hashing the password before saving the user
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

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

    public Optional<User> authenticate(String username, String rawPassword) {
        return userRepository.findByUsername(username)
                .filter(user -> passwordEncoder.matches(rawPassword, user.getPassword()));
    }

    public boolean roleExists(String roleName) {
        return roleRepository.findByName(roleName) != null;
    }

    public List<Agent> getAgentsByUser(Long userId) {
        return agentRepository.findByUserId(userId);
    }
}
