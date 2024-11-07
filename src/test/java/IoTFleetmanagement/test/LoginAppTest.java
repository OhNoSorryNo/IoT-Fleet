package IoTFleetmanagement.test;


import IoTFleetManagement.LoginApp;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Test class for the LoginApp Spring Boot application.
 * Ensures that the application context loads without issues.
 */
@SpringBootTest
class LoginAppTest {

    @Test
    void contextLoads() {
        // Test that the application context loads without throwing exceptions
        assertDoesNotThrow(() -> LoginApp.main(new String[] {}));
    }
}
