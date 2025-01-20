package IoTFleetManagment.test.security.config;

import IoTFleetManagement.security.config.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {

    private static JwtUtil jwtUtil;

    @BeforeAll
    static void setUp() {
//        try (MockedStatic<System> mockedSystem = Mockito.mockStatic(System.class)) {
//            mockedSystem.when(() -> System.getenv("SECRET_TOKEN")).thenReturn("mytestsecretkey1234567890123456");
//            jwtUtil = new JwtUtil();
//        }
        // Now this line will actually be picked up in the static block
        String longSecret = "abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ1234";
        System.setProperty("SECRET_TOKEN", longSecret);
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
        String subject = "test-agent-id";
        String token = JwtUtil.generateToken(subject);

        boolean isValid = jwtUtil.validateToken(token, subject);

        assertTrue(isValid);
    }

    @Test
    public void testValidateToken_InvalidToken() {
        String invalidToken = "invalid.token.value";

        boolean isValid = jwtUtil.validateToken(invalidToken, "test-agent-id");

        assertFalse(isValid);
    }

    @Test
    public void testValidateToken_WrongAgentId() {
        String subject = "test-agent-id";
        String token = JwtUtil.generateToken(subject);

        boolean isValid = jwtUtil.validateToken(token, "wrong-agent-id");

        assertFalse(isValid);
    }
}
