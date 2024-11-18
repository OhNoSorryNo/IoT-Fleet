package IoTFleetManagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * Main application class for the Login module of the IoT Fleet Management System.
 * This class serves as the entry point for the Spring Boot application.
 *
 * @author struckmeie
 * @author jasmin1707
 */
@SpringBootApplication
//@EntityScan("IoTFleetManagement.model")
public class IoTApp {
    public static void main(String[] args) {
        // Launches the Spring Boot application
        SpringApplication.run(IoTApp.class, args);
    }
}
