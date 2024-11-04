package IoTFleetManagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers("/auth/register", "/auth/login", "/index.html", "/css/**", "/js/**", "/")
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers( "/", "/index.html", "/css/**", "/auth/register", "/csrf-token", "/auth/login", "/js/**").permitAll() // Allow anyone to access /auth/register
                        .anyRequest().authenticated() // Require authentication for all other endpoints
                )

                .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
        );

        return http.build();
    }
}
