package IoTFleetManagement.security.config;

import IoTFleetManagement.user.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration class for setting up application security using Spring Security.
 * <p>
 * This class defines the security settings, including endpoint access rules, CSRF protection,
 * session management policies, and the authentication provider.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    /**
     * Constructor for injecting the {@link CustomUserDetailsService}.
     *
     * @param userDetailsService the service used to load user details for authentication
     */
    // Constructor injection for CustomUserDetailsService
    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * Configures the application's security settings.
     * <p>
     * The configuration includes:
     * <ul>
     *     <li>Forcing HTTPS for all requests.</li>
     *     <li>Disabling CSRF protection for simplicity (not recommended for production).</li>
     *     <li>Defining public endpoints that do not require authentication.</li>
     *     <li>Requiring authentication for all other endpoints.</li>
     *     <li>Setting session management policy to {@code IF_REQUIRED} to reduce session creation.</li>
     * </ul>
     *
     * @param http the {@link HttpSecurity} instance used to configure security
     * @return a {@link SecurityFilterChain} defining the security rules
     * @throws Exception if an error occurs while configuring security
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                //Enforcing HTTPS for all requests.
                .requiresChannel(channel -> channel
                        .anyRequest().requiresSecure()
                )
//                .csrf(csrf -> csrf
//                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
//                        .ignoringRequestMatchers("/auth/register", "/index.html", "/css/**", "/js/**", "/", "/languages/**", "/auth/login", "/agents/**", "/dashboard.html")
//                )
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers( "/", "/index.html", "/css/**", "/auth/register", "/csrf-token", "/js/**", "/languages/**", "/auth/login", "/register.html", "/agents/**", "/dashboard.html").permitAll() // Allow anyone to access
                        .anyRequest().authenticated() // Require authentication for all other endpoints
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // Use IF_REQUIRED to avoid excessive session creation
                )
                .authenticationProvider(authenticationProvider());
        return http.build();
    }

    /**
     * Creates an {@link AuthenticationProvider} bean for authenticating users.
     * <p>
     * This provider uses a {@link DaoAuthenticationProvider} with a custom user details service
     * and a password encoder for secure authentication.
     *
     * @return an {@link AuthenticationProvider} instance
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Creates a {@link PasswordEncoder} bean for encoding passwords.
     * <p>
     * The password encoder uses the {@link BCryptPasswordEncoder} to securely hash passwords.
     *
     * @return a {@link PasswordEncoder} instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
