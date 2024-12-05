package IoTFleetManagement.security.config;

import ch.qos.logback.classic.Logger;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.LoggerFactory;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for managing JSON Web Tokens (JWTs) in the IoT Fleet Management system.
 * <p>
 * This class provides methods for generating and validating JWTs used for secure communication
 * between the system and IoT agents.
 * </p>
 *
 * @author Lara
 * @author Jasmin1707
 */
@Component
public class JwtUtil {

    private static final Logger log = (Logger) LoggerFactory.getLogger(JwtUtil.class);

    private static final String SECRET = System.getenv("SECRET_TOKEN");
    private static final SecretKey SECRET_KEY;

    static {
        if (SECRET != null && !SECRET.isEmpty()) {
            SECRET_KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
            log.info("SECRET_TOKEN environment variable set successfully.");
        } else {
            throw new IllegalStateException("SECRET_TOKEN environment variable not set");
        }
    }

    /**
     * Generates a JWT for the given subject (e.g., an agent ID).
     *
     * @param subject the subject (e.g., agent ID) to be included in the token
     * @return a compact JWT string signed with the system's secret key
     */
    public static String generateToken(String subject) {
        log.debug("Generating token for subject: {}", subject);
        return Jwts.builder()
                .setSubject(subject)
                .signWith(SECRET_KEY, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Validates a JWT token against the given agent ID.
     *
     * @param token the JWT token to be validated
     * @param agentId the agent ID to validate against
     * @return {@code true} if the token is valid and matches the agent ID, {@code false} otherwise
     */
    public boolean validateToken(String token, String agentId) {
        try {
            log.debug("Validating token for agentId: {}", agentId);
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();

            boolean isValid = claims.getSubject().equals(agentId);
            log.debug("Token validation result for agentId {}: {}", agentId, isValid);
            return isValid;
        } catch (ExpiredJwtException e) {
            log.warn("Token has expired for agentId: {}", agentId);
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token for agentId: {}", agentId);
        } catch (MalformedJwtException e) {
            log.error("Malformed JWT token for agentId: {}", agentId);
        } catch (SignatureException e) {
            log.error("Invalid signature for token with agentId: {}", agentId);
        } catch (Exception e) {
            log.error("JWT validation failed for agentId: {}: {}", agentId, e.getMessage());
        }
        return false;
    }
}
