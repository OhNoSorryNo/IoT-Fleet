package IoTFleetManagement.security.config;

import ch.qos.logback.classic.Logger;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

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
        } catch (ExpiredJwtException e) {
        // Token ist abgelaufen
        log.warn("Token für Agent {} ist abgelaufen: {}", agentId, e.getMessage());
        return false;
    } catch (UnsupportedJwtException e) {
        // Nicht unterstütztes JWT
        log.warn("Nicht unterstütztes JWT für Agent {}: {}", agentId, e.getMessage());
        return false;
    } catch (MalformedJwtException e) {
        // Fehlerhaftes JWT
        log.warn("Fehlerhaftes JWT für Agent {}: {}", agentId, e.getMessage());
        return false;
    } catch (SignatureException e) {
        // Signaturvalidierung fehlgeschlagen
        log.warn("Ungültige Signatur für Agent {}: {}", agentId, e.getMessage());
        return false;
    } catch (IllegalArgumentException e) {
        // Token ist null oder leer
        log.warn("Token ist null oder leer für Agent {}: {}", agentId, e.getMessage());
        return false;
    }
    }
}
