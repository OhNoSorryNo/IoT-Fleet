package IoTFleetmanagement.test;


import IoTFleetManagement.IoTApp;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Test class for the LoginApp Spring Boot application.
 * Ensures that the application context loads without issues.
 */
@SpringBootTest
class IoTAppTest {

    @Test
    void contextLoads() {
        // Test that the application context loads without throwing exceptions
        assertDoesNotThrow(() -> IoTApp.main(new String[] {}));
    }
}
