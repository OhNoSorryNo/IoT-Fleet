package IoTFleetmanagement.test.security.config;

import IoTFleetManagement.security.config.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {

    private static JwtUtil jwtUtil;

    @BeforeAll
    static void setUp() {
        // Set the environment variable for the secret token (this is just for testing purposes)
        System.setProperty("SECRET_TOKEN", "mytestsecretkey1234567890123456");
        jwtUtil = new JwtUtil();
    }

    @Test
    public void testGenerateToken() {
        // Arrange
        String subject = "test-agent-id";

        // Act
        String token = JwtUtil.generateToken(subject);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    public void testValidateToken_ValidToken() {
        // Arrange
        String subject = "test-agent-id";
        String token = JwtUtil.generateToken(subject);

        // Act
        boolean isValid = jwtUtil.validateToken(token, subject);

        // Assert
        assertTrue(isValid);
    }

    @Test
    public void testValidateToken_InvalidToken() {
        // Arrange
        String invalidToken = "invalid.token.value";

        // Act
        boolean isValid = jwtUtil.validateToken(invalidToken, "test-agent-id");

        // Assert
        assertFalse(isValid);
    }

    @Test
    public void testValidateToken_WrongAgentId() {
        // Arrange
        String subject = "test-agent-id";
        String token = JwtUtil.generateToken(subject);

        // Act
        boolean isValid = jwtUtil.validateToken(token, "wrong-agent-id");

        // Assert
        assertFalse(isValid);
    }
}
