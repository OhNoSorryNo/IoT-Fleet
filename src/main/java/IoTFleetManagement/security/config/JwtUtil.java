package IoTFleetManagement.security.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {
    private static final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS512); // Replace with a secure key in production

    public static String generateToken(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .signWith(SECRET_KEY, SignatureAlgorithm.HS512)
                .compact();
    }

    public static boolean validateToken(String token, String agentId) {
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
