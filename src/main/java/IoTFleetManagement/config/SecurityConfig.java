package IoTFleetManagement.config;

        import org.springframework.context.annotation.Bean;
        import org.springframework.context.annotation.Configuration;
        import org.springframework.security.config.annotation.web.builders.HttpSecurity;
        import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
        import org.springframework.security.config.http.SessionCreationPolicy;
        import org.springframework.security.web.SecurityFilterChain;
        import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    /**
     * Configures the security settings for the application.
     * This includes CSRF protection, session management, and defining which endpoints are publicly accessible.
     *
     * @param http the HttpSecurity instance used to configure security settings
     * @return a configured SecurityFilterChain instance
     * @throws Exception if an error occurs while configuring security
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers("/auth/register", "/auth/login", "/index.html", "/css/**", "/js/**", "/", "languages")
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers( "/", "/index.html", "/css/**", "/auth/register", "/csrf-token", "/auth/login", "/js/**", "languages").permitAll() // Allow anyone to access /auth/register
                        .anyRequest().authenticated() // Require authentication for all other endpoints
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // Use IF_REQUIRED to avoid excessive session creation
                );
        return http.build();
    }
}
