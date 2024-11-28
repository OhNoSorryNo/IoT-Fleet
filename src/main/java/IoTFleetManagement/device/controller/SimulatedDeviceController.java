package IoTFleetManagement.device.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/simulated-device")
public class    SimulatedDeviceController {

    private static final Logger logger = LoggerFactory.getLogger(SimulatedDeviceController.class);


    @GetMapping("/heartbeat")
    public ResponseEntity<String> getStatus() {
        logger.info("Received ping from server");
        return ResponseEntity.ok("Device is online");
    }
}
