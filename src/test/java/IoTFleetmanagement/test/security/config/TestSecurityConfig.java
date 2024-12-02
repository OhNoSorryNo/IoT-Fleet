package IoTFleetmanagement.test.security.config;

import IoTFleetManagement.user.service.CustomUserDetailsService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.mockito.Mockito.mock;

/**
 * Test security configuration class for the IoT Fleet Management system.
 * <p>
 * This configuration is used during testing to simplify security settings,
 * such as disabling CSRF and allowing all requests without authentication.
 */
@TestConfiguration
public class TestSecurityConfig {

    /**
     * Provides a mock implementation of CustomUserDetailsService for testing.
     *
     * @return a mock CustomUserDetailsService
     */
    @Bean
    public CustomUserDetailsService customUserDetailsService() {
        return mock(CustomUserDetailsService.class); // Mocked CustomUserDetailsService for testing
    }

    /**
     * Provides a password encoder bean for testing.
     *
     * @return a PasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Use the same password encoder as in production
    }

    /**
     * Configures an authentication provider for testing purposes.
     *
     * @return the configured AuthenticationProvider
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Configures a security filter chain for testing purposes.
     *
     * <p>Disables CSRF protection and permits all requests without requiring authentication.</p>
     *
     * @param http the {@link HttpSecurity} to be configured
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if an error occurs while configuring the security settings
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()) // Disable CSRF for testing
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())  // Allow all requests without authentication
                .authenticationProvider(authenticationProvider()); // Include the authentication provider for completeness
        return http.build();
    }
}
