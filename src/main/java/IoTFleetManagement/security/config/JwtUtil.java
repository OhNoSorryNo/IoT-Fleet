package IoTFleetManagement.security.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Utility class for managing JSON Web Tokens (JWTs) in the IoT Fleet Management system.
 * <p>
 * This class provides methods for generating and validating JWTs used for secure communication
 * between the system and IoT agents.
 * </p>
 */
@Component
public class JwtUtil {
    private static final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS512); // Replace with a secure key in production

    /**
     * Generates a JWT for the given subject (e.g., an agent ID).
     *
     * @param subject the subject (e.g., agent ID) to be included in the token
     * @return a compact JWT string signed with the system's secret key
     */
    public static String generateToken(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .signWith(SECRET_KEY, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Validates a given JWT against the expected subject (agent ID).
     *
     * <p>This method parses the provided JWT using the system's secret key and verifies that the token's subject
     * matches the specified agent ID. If the token is invalid or cannot be parsed, the method returns {@code false}.</p>
     *
     * @param token   the JWT to validate
     * @param agentId the expected subject (agent ID) contained within the token
     * @return {@code true} if the token is valid and matches the expected agent ID, {@code false} otherwise
     */
    public boolean validateToken(String token, String agentId) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject().equals(agentId);
        } catch (Exception e) {
            return false;
        }
    }
}
