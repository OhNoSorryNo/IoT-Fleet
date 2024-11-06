package IoTFleetManagement.service;

import IoTFleetManagement.exceptions.UsernameAlreadyExistsException;
import IoTFleetManagement.model.Role;
import IoTFleetManagement.model.User;
import IoTFleetManagement.repository.RoleRepository;
import IoTFleetManagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public User registerUser(String username, String password, String roleName) {
        // Check if the username already exists
        if (userRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException("Username '" + username + "' is already taken.");
        }
        Role role = roleRepository.findRoleByName(roleName); // Find the role by name
        User user = new User(username, password, role);
        return userRepository.save(user);
    }

    public Optional<User> authenticate(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> user.getPassword().equals(password)); // Simple password check
    }

    public boolean roleExists(String roleName) {
        return roleRepository.findRoleByName(roleName) != null;
    }

}
