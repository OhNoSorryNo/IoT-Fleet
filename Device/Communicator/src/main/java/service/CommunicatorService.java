package service;

import model.Communicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Profile("simulated")
public class CommunicatorService implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(CommunicatorService.class);
    private final RestTemplate restTemplate;
    private final String deviceId = System.getenv("DEVICE_ID");
    private final String secretKey = System.getenv("SECRET_KEY");
    private final Communicator communicator = new Communicator(deviceId, secretKey);

    @Value("${communicator.backend.url}")
    String backendUrl;

    @Value("${communicator.updater.url}")
    String updaterUrl;

    @Value("${communicator.led.url}")
    private String ledUrl;

    private boolean isRegistered = false;
    private String token = null;

    public CommunicatorService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        logger.info("SimulatedDeviceService initialized");
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        logger.info("Application is ready, attempting to register device.");
        sendLedStatus("registration");
        registerDevice();
        logger.info("Sending initial heartbeat after registration attempt.");
        sendHeartbeat();
    }

    private synchronized void registerDevice() {
        logger.debug("Preparing registration request for device: {}", communicator.getDeviceId());
        Map<String, Object> request = new HashMap<>();
        request.put("agentId", communicator.getDeviceId());
        request.put("secretKey", communicator.getSecretKey());

        int retryCount = 0;

        while (!isRegistered && retryCount < 2) {
            try {
                logger.info("Checking if device is already registered: {} (Attempt {})", communicator.getDeviceId(), retryCount + 1);
                ResponseEntity<Boolean> checkResponse = restTemplate.getForEntity(
                        backendUrl + "/" + communicator.getDeviceId() + "/exists", Boolean.class
                );

                if (Boolean.TRUE.equals(checkResponse.getBody())) {
                    logger.info("Device already registered: {}", communicator.getDeviceId());
                    ResponseEntity<Map> response = restTemplate.postForEntity(backendUrl + "/getToken", request, Map.class);
                    Map responseBody = response.getBody();
                    if (responseBody != null && responseBody.containsKey("token")) {
                        String token = (String) responseBody.get("token");
                        communicator.setJwtToken(token);
                        this.token = token;
                        logger.info("Device successfully reconnected");
                    }
                    isRegistered = true;
                    sendLedStatus("successful");
                    break;
                }
                logger.debug("Device not registered, sending registration request...");
                ResponseEntity<Map> response = restTemplate.postForEntity(backendUrl + "/register", request, Map.class);
                Map responseBody = response.getBody();
                logger.debug("Registration response body: {}", responseBody);
                if (responseBody != null && responseBody.containsKey("token")) {
                    String token = (String) responseBody.get("token");
                    communicator.setJwtToken(token);
                    this.token = token;
                    logger.info("Device registered successfully");
                    isRegistered = true;
                    sendLedStatus("successful");
                } else {
                    throw new RuntimeException("Registration response does not contain a valid token.");
                }
            } catch (Exception e) {
                logger.error("Failed to register device. Retrying... ({})", ++retryCount, e);
                sendLedStatus("unsuccessful");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ignored) {
                    logger.warn("Thread sleep interrupted during registration retry.");
                }
            }
        }

        if (!isRegistered) {
            logger.error("Device registration failed after multiple attempts.");
            sendLedStatus("unsuccessful");
        }
    }

    @Scheduled(fixedRate = 10000)
    public void sendHeartbeat() {
        logger.info("Heartbeat triggered.");
        if (!isRegistered) {
            logger.warn("Device is not registered. Skipping heartbeat.");
            registerDevice();
            return;
        }

        logger.debug("Preparing heartbeat request for device: {}", communicator.getDeviceId());
        Map<String, Object> request = Map.of("online", true);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            restTemplate.put(backendUrl + "/status" + "/" + communicator.getDeviceId(), entity);
            logger.info("Heartbeat sent for agent: {}", communicator.getDeviceId());
            sendLedStatus("online");
        } catch (Exception e) {
            logger.error("Failed to send heartbeat: {}", e.getMessage());
            sendLedStatus("offline");
        }
    }

    @Scheduled(fixedRate = 20000)
    public void checkForUpdate() {
        if (!isRegistered) return;

        String url = backendUrl + "/" + communicator.getDeviceId() + "/update-check";
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
                sendLedStatus("updating");
                sendUpdateStatus(true);
            } else {
                logger.warn("Firmware update failed. Status: {}", response.getStatusCode());
                sendLedStatus("unsuccessful");
                sendUpdateStatus(false);
            }
        } catch (Exception e) {
            logger.error("Error sending update request to updater: {}", e.getMessage());
            sendLedStatus("unsuccessful");
            sendUpdateStatus(false);
        }
    }

    public void sendUpdateStatus(boolean success) {
        String statusUrl = backendUrl + "/" + communicator.getDeviceId() + "/update-status";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("status", success ? "success" : "failure");
        requestBody.put("deviceId", communicator.getDeviceId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(statusUrl, HttpMethod.PUT, requestEntity, String.class);
            logger.info("Update status sent to server: {}", success ? "success" : "failure");
        } catch (Exception e) {
            logger.error("Failed to send update status: {}", e.getMessage());
        }
    }

    private void sendLedStatus(String status) {
        Map<String, String> requestBody = Map.of("status", status);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(ledUrl, requestEntity, String.class);
            logger.info("LED status updated: {}", status);
        } catch (Exception e) {
            logger.error("Failed to update LED status: {}", e.getMessage());
        }
    }
}
