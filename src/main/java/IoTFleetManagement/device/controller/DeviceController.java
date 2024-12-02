package IoTFleetManagement.device.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/device")
public class DeviceController {

    @Value("${device.code:Unknown}")
    private String deviceCode;

    private LocalDateTime lastPingTime = null;
    private boolean online = false;

    // Endpoint to receive a ping and update the last ping time
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        lastPingTime = LocalDateTime.now();
        online = true;
        String response = "Device " + deviceCode + " received ping and is online";
        return ResponseEntity.ok(response);
    }

    // Endpoint to provide the current status of the device
    @GetMapping("/status")
    public ResponseEntity<String> getStatus() {
        String status = online ? "Online" : "Offline";
        return ResponseEntity.ok("Device " + deviceCode + " status: " + status);
    }

    // Scheduled method to check if the device is still online
    @Scheduled(fixedRate = 30000)
    public void checkDeviceStatus() {
        if (lastPingTime != null) {
            long secondsSinceLastPing = ChronoUnit.SECONDS.between(lastPingTime, LocalDateTime.now());
            if (secondsSinceLastPing > 60) {
                online = false;
            }
        } else {
            online = false;
        }
    }

    // Endpoint to receive updates (simulation)
    @PutMapping("/update")
    public ResponseEntity<String> updateDevice(@RequestParam String newFirmwareVersion) {
        String response = "Device " + deviceCode + " updated to firmware version " + newFirmwareVersion;
        return ResponseEntity.ok(response);
    }
}
