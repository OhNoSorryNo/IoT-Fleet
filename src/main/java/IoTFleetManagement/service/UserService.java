package IoTFleetManagement.service;

import IoTFleetManagement.model.Role;
import IoTFleetManagement.model.User;
import IoTFleetManagement.repository.RoleRepository;
import IoTFleetManagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User saveUser(User user) {
        // Hash das Passwort bevor du den Benutzer speicherst
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User registerUser(String username, String password, String roleName) {
        Role role = roleRepository.findByName(roleName);
        // Hash das Passwort
        String hashedPassword = passwordEncoder.encode(password);
        User user = new User(username, hashedPassword, role);
        return userRepository.save(user);
    }

    public Optional<User> authenticate(String username, String rawPassword) {
        return userRepository.findByUsername(username)
                .filter(user -> passwordEncoder.matches(rawPassword, user.getPassword()));
    }

    public boolean roleExists(String roleName) {
        return roleRepository.findByName(roleName) != null;
    }
}
