package IoTFleetManagement.user.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    // Implement the methods from UserDetails


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Role role = user.getRole();
        String roleName = role.getName();
        return Collections.singletonList(new SimpleGrantedAuthority(roleName));
    }

    @Override
    public String getPassword() {
        return user.getPassword(); // Return the password from your User entity
    }

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

    // Getter for the User entity
    public User getUser() {
        return user;
    }
}