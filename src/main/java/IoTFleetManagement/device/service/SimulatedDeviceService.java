package IoTFleetManagement.device.service;

import IoTFleetManagement.device.model.SimulatedDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import java.util.HashMap;
import java.util.Map;

@Service
@Profile("simulated")
public class SimulatedDeviceService implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(SimulatedDeviceService.class);
    private final RestTemplate restTemplate;
    private final SimulatedDevice simulatedDevice = new SimulatedDevice("imaginaryDevice01", "secureKey01");
    private final String backendUrl = "https://localhost:8443/agents";
    private boolean isRegistered = false;
    private String token = null;

    public SimulatedDeviceService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Registers the simulated device using the /agents endpoint after the application is ready.
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        registerDevice();
    }

    private synchronized void registerDevice() {
        Map<String, Object> request = new HashMap<>();
        request.put("agentId", simulatedDevice.getDeviceId());
        request.put("secretKey", simulatedDevice.getSecretKey());

        int retryCount = 0;

        while (!isRegistered && retryCount < 2) { // Retry up to 5 times
            try {
                // Check if the agent already exists
                ResponseEntity<Boolean> checkResponse = restTemplate.getForEntity(
                        backendUrl + "/" + simulatedDevice.getDeviceId() + "/exists", Boolean.class
                );

                if (Boolean.TRUE.equals(checkResponse.getBody())) {
                    logger.info("Device already registered: {}", simulatedDevice.getDeviceId());
                    isRegistered = true;
                    break;
                }
                logger.debug("Sending registration request...");
                // Send registration request
                ResponseEntity<Map> response = restTemplate.postForEntity(backendUrl + "/register", request, Map.class);

                // Parse the response to retrieve the token
                Map responseBody = response.getBody();
                logger.debug("Response body: {}", responseBody);
                if (responseBody != null && responseBody.containsKey("token")) {
                    token = (String) responseBody.get("token");
                    logger.info("Device registered successfully: {} with token: {}", simulatedDevice.getDeviceId(), token);
                    isRegistered = true;
                } else {
                    throw new RuntimeException("Registration response does not contain a valid token.");
                }
            } catch (Exception e) {
                logger.error("Failed to register device. Retrying... ({})", ++retryCount, e);
                try {
                    Thread.sleep(3000); // Wait 3 seconds before retrying
                } catch (InterruptedException ignored) {
                }
            }
        }

        if (!isRegistered) {
            logger.error("Device registration failed after multiple attempts.");
        }
    }

    /**
     * Sends a heartbeat to update the device's status using the PUT /agents/{agentId}/status endpoint.
     */
    @Scheduled(fixedRate = 30000) // Every 30 seconds
    public void sendHeartbeat() {
        if (!isRegistered) {
            logger.warn("Device is not registered. Skipping heartbeat.");
            return;
        }

        Map<String, Object> request = Map.of("online", true);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);


        try {
            restTemplate.put(backendUrl + "/" + simulatedDevice.getDeviceId() + "/status", request);
            logger.info("Heartbeat sent for agent: {}", simulatedDevice.getDeviceId());
        } catch (Exception e) {
            logger.error("Failed to send heartbeat: {}", e.getMessage());
        }
    }
}