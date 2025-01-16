package service;

import model.SimulatedDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Service class for managing the simulated IoT device in the fleet management system.
 * <p>
 * This class provides functionality for registering the device and sending heartbeat signals
 * to update the device's status in the backend system.
 * </p>
 *
 * @author ...
 */
@Service
@Profile("simulated")
public class SimulatedDeviceService implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(SimulatedDeviceService.class);
    private final RestTemplate restTemplate;
    private final String deviceId = System.getenv("DEVICE_ID");
    private final String secretKey = System.getenv("SECRET_KEY");
    private final SimulatedDevice simulatedDevice = new SimulatedDevice(deviceId, secretKey);
    private final String backendUrl = "https://server-app:8443/agents";
    String updaterUrl = "http://updater:9090/update-image";
    private boolean isRegistered = false;
    private String token = null;

    /**
     * Constructor to initialize the SimulatedDeviceService with a RestTemplate.
     *
     * @param restTemplate the RestTemplate used for making HTTP requests
     */
    public SimulatedDeviceService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        logger.info("SimulatedDeviceService initialized");
    }

    /**
     * Registers the simulated device using the /agents endpoint after the application is ready.
     * <p>
     * This method is triggered automatically when the application context is fully initialized.
     * It attempts to register the device and then sends an initial heartbeat.
     * </p>
     *
     * @param event the event triggered when the application is ready
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        logger.info("Application is ready, attempting to register device.");
        registerDevice();
        logger.info("Sending initial heartbeat after registration attempt.");
        sendHeartbeat();
    }

    /**
     * Registers the simulated device with the backend service.
     * <p>
     * This method sends a registration request to the backend. If the device is already registered,
     * it attempts to retrieve a new token for further communication. The method will retry registration
     * up to two times if it fails initially.
     * </p>
     */
    private synchronized void registerDevice() {
        logger.debug("Preparing registration request for device: {}", simulatedDevice.getDeviceId());
        Map<String, Object> request = new HashMap<>();
        request.put("agentId", simulatedDevice.getDeviceId());
        request.put("secretKey", simulatedDevice.getSecretKey());

        int retryCount = 0;

        while (!isRegistered && retryCount < 2) { // Retry up to 2 times
            try {
                logger.info("Checking if device is already registered: {} (Attempt {})", simulatedDevice.getDeviceId(), retryCount + 1);
                // Check if the agent already exists
                ResponseEntity<Boolean> checkResponse = restTemplate.getForEntity(
                        backendUrl + "/" + simulatedDevice.getDeviceId() + "/exists", Boolean.class
                );

                if (Boolean.TRUE.equals(checkResponse.getBody())) {
                    logger.info("Device already registered: {}", simulatedDevice.getDeviceId());
                    ResponseEntity<Map> response = restTemplate.postForEntity(backendUrl + "/getToken", request, Map.class);
                    Map responseBody = response.getBody();
                    if (responseBody != null && responseBody.containsKey("token")) {
                        String token = (String) responseBody.get("token");
                        simulatedDevice.setJwtToken(token);
                        this.token = token;
                        logger.info("Device successfully reconnected");
                    }
                    isRegistered = true;
                    break;
                }
                logger.debug("Device not registered, sending registration request...");
                ResponseEntity<Map> response = restTemplate.postForEntity(backendUrl + "/register", request, Map.class);
                Map responseBody = response.getBody();
                logger.debug("Registration response body: {}", responseBody);
                if (responseBody != null && responseBody.containsKey("token")) {
                    String token = (String) responseBody.get("token");
                    simulatedDevice.setJwtToken(token);
                    this.token = token;
                    logger.info("Device registered successfully");
                    isRegistered = true;
                } else {
                    throw new RuntimeException("Registration response does not contain a valid token.");
                }
            } catch (Exception e) {
                logger.error("Failed to register device. Retrying... ({})", ++retryCount, e);
                try {
                    Thread.sleep(3000); // Wait 3 seconds before retrying
                } catch (InterruptedException ignored) {
                    logger.warn("Thread sleep interrupted during registration retry.");
                }
            }
        }

        if (!isRegistered) {
            logger.error("Device registration failed after multiple attempts.");
        }
    }

    /**
     * Sends a heartbeat to update the device's status using the PUT /agents/{agentId}/status endpoint.
     * <p>
     * This method is scheduled to run at a fixed rate of every 30 seconds.
     * It sends the current status of the device to the backend service.
     * </p>
     */
    @Scheduled(fixedRate = 10000) // Every 10 seconds
    public void sendHeartbeat() {
        logger.info("Heartbeat triggered.");
        if (!isRegistered) {
            logger.warn("Device is not registered. Skipping heartbeat.");
            registerDevice();
            logger.info("Registration triggered.");

            return;
        }

        logger.debug("Preparing heartbeat request for device: {}", simulatedDevice.getDeviceId());
        Map<String, Object> request = Map.of("online", true);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            restTemplate.put(backendUrl + "/status" + "/" + simulatedDevice.getDeviceId(), entity);
            logger.info("Heartbeat sent for agent: {}", simulatedDevice.getDeviceId());
        } catch (Exception e) {
            logger.error("Failed to send heartbeat: {}", e.getMessage());
        }
    }

    /**
     * Sends an update check request to verify if a firmware update is required.
     * <p>
     * This method is scheduled to run at a fixed rate and sends the current firmware version
     * of the device to the backend. If an update is available, it processes the response
     * and logs the update URL.
     * </p>
     */

    @Scheduled(fixedRate = 20000)
    public void checkForUpdate() {
        if (!isRegistered) return;

        String url = backendUrl + "/" + simulatedDevice.getDeviceId() + "/update-check";
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                if ((Boolean) responseBody.get("status")) {
                    String registryUrl = (String) responseBody.get("registry_url");
                    String imageName = (String) responseBody.get("image_name");
                    String tag = (String) responseBody.get("tag");
                    sendUpdateRequestToUpdater(registryUrl, imageName, tag);
                }
            }
        } catch (Exception e) {
            logger.error("Error checking for firmware update: {}", e.getMessage());
        }
    }

    public void sendUpdateRequestToUpdater(String registryUrl, String imageName, String tag) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("registry_url", registryUrl);
        requestBody.put("image_name", imageName);
        requestBody.put("tag", tag);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    updaterUrl,
                    HttpMethod.PUT,
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Firmware update triggered successfully.");
                sendUpdateStatus(true);
            } else {
                logger.warn("Firmware update failed. Status: {}", response.getStatusCode());
                sendUpdateStatus(false);
            }
        } catch (Exception e) {
            logger.error("Error sending update request to updater: {}", e.getMessage());
            sendUpdateStatus(false);
        }
    }



    public boolean performUpdate(String updateUrl) {
        try {
            String command = "curl -w \"%{http_code}\" -o /dev/null -s -X PUT -H \"Content-Type: application/json\" -d '{\"image_name\": \""+ updateUrl +"\"}' ";
            Process process = Runtime.getRuntime().exec(command);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;

            // Read the HTTP status code returned by curl
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            int exitCode = process.waitFor();

            // Parse the HTTP status code from the curl output
            String statusCode = output.toString().trim();
            if (exitCode == 0 && statusCode.startsWith("2")) {
                logger.error("Update completed successfully for URL: {}. HTTP Status Code: {}", updateUrl, statusCode);
                return true;
            } else {
                logger.error("Update failed for URL: {}. HTTP Status Code: {}", updateUrl, statusCode);
                return false;
            }
        } catch (Exception e) {
            logger.error("Error during update execution for URL: {}. Exception: {}", updateUrl, e.getMessage());
            return false;
        }
    }

    public void sendUpdateStatus(boolean success) {
        String statusUrl = backendUrl + "/" + simulatedDevice.getDeviceId() + "/update-status";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("status", success ? "success" : "failure");
        requestBody.put("deviceId", simulatedDevice.getDeviceId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            restTemplate.exchange(statusUrl, HttpMethod.PUT, requestEntity, Void.class);
            logger.info("Update status sent to server: {}", success ? "success" : "failure");
        } catch (Exception e) {
            logger.error("Failed to send update status: {}", e.getMessage());
        }
    }

}
