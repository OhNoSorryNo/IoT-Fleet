package IoTFleetManagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * Main application class for the Login module of the IoT Fleet Management System.
 * This class serves as the entry point for the Spring Boot application.
 *
 * @author struckmeie
 * @author jasmin1707
 */
@SpringBootApplication(scanBasePackages = "IoTFleetManagement")
@EnableScheduling
//@EntityScan("IoTFleetManagement.model")
public class IoTApp {
    public static void main(String[] args) {
        // Launches the Spring Boot application
        SpringApplication.run(IoTApp.class, args);
    }
}
