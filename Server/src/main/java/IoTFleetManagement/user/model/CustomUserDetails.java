package IoTFleetManagement.user.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;

/**
 * Custom implementation of {@link UserDetails} for integrating the {@link User} entity with Spring Security.
 * <p>
 * This class adapts the {@link User} entity to provide the necessary details for authentication and authorization.
 * It encapsulates the user's credentials, roles.
 *
 * @author Lara
 * @author Jasmin1707
 */
public class CustomUserDetails implements UserDetails {

    private final User user;

    /**
     * Constructs a new {@code CustomUserDetails} with the given {@link User}.
     *
     * @param user the {@link User} entity to wrap
     */
    public CustomUserDetails(User user) {
        this.user = user;
    }


    /**
     * Returns the authorities (roles) granted to the user.
     *
     * @return a collection containing a single {@link SimpleGrantedAuthority} derived from the user's role
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Role role = user.getRole();
        String roleName = role.getName();
        return Collections.singletonList(new SimpleGrantedAuthority(roleName));
    }

    /**
     * Returns the user's password for authentication.
     *
     * @return the password of the user
     */
    @Override
    public String getPassword() {
        return user.getPassword(); // Return the password from your User entity
    }

    /**
     * Returns the user's username for authentication.
     *
     * @return the username of the user
     */
    @Override
    public String getUsername() {
        return user.getUsername(); // Return the username from your User entity
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Modify if you have account expiration logic
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Modify if you have account locking logic
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Modify if you have credential expiration logic
    }

    @Override
    public boolean isEnabled() {
        return true; // Modify if you track whether the user is enabled
    }

    /**
     * Returns the wrapped {@link User} entity.
     *
     * @return the underlying {@link User} instance
     */
    public User getUser() {
        return user;
    }
}