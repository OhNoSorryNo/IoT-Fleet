package IoTFleetManagement.security.config;

import ch.qos.logback.classic.Logger;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.LoggerFactory;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

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
            log.info("SECRET_TOKEN environment variable set to: {}", SECRET);
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
        return Jwts.builder()
                .setSubject(subject)
                .signWith(SECRET_KEY, SignatureAlgorithm.HS512)
                .compact();
    }

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
