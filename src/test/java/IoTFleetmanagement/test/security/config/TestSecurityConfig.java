package IoTFleetmanagement.test.security.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Test security configuration class for the IoT Fleet Management system.
 * <p>
 * This configuration is used during testing to simplify security settings,
 * such as disabling CSRF and allowing all requests without authentication.
 */
@TestConfiguration
public class TestSecurityConfig {

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
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()); // Allow all requests without authentication
        return http.build();
    }
}
