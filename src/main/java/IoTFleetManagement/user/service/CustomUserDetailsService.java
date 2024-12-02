package IoTFleetManagement.user.service;

import IoTFleetManagement.user.model.User;
import IoTFleetManagement.user.repository.UserRepository;
import IoTFleetManagement.user.model.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link UserDetailsService} for loading user-specific data.
 * <p>
 * This service integrates with the {@link UserRepository} to retrieve user details
 * and adapts them into a {@link CustomUserDetails} object for Spring Security.
 *
 * @author Lara
 * @author Jasmin1707
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Constructs a new {@code CustomUserDetailsService} with the specified {@link UserRepository}.
     *
     * @param userRepository the repository for accessing user data
     */
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user by their username.
     * <p>
     * This method retrieves the user from the database using the {@link UserRepository}.
     * If the user is found, it wraps the user entity in a {@link CustomUserDetails} object.
     * If the user is not found, a {@link UsernameNotFoundException} is thrown.
     *
     * @param username the username of the user to be loaded
     * @return a {@link CustomUserDetails} object containing user details
     * @throws UsernameNotFoundException if no user is found with the given username
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Load user from the database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Return a CustomUserDetails object
        return new CustomUserDetails(user);
    }
}