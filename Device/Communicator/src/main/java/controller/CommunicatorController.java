package controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller class for managing simulated IoT devices in the fleet management system.
 * <p>
 * This class provides REST API endpoints to manage the simulated IoT devices,
 * including sending heartbeat signals to indicate that the device is online.
 * </p>
 *
 * @author Lara
 * @author Jasmin1707
 */
@RestController
@RequestMapping("/simulated-device")
public class CommunicatorController {

    private static final Logger logger = LoggerFactory.getLogger(CommunicatorController.class);

    /**
     * Endpoint to simulate a heartbeat signal from the device.
     * <p>
     * This method logs the receipt of a ping request and returns a response indicating that the device is online.
     * </p>
     *
     * @return a ResponseEntity containing the message "Device is online"
     */
    @GetMapping("/heartbeat")
    public ResponseEntity<String> getStatus() {
        logger.info("Received heartbeat ping from simulated device");
        return ResponseEntity.ok("Device is online");
    }


}
