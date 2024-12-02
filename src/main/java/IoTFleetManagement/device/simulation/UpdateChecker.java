package IoTFleetManagement.device.simulation;

import IoTFleetManagement.device.utils.HttpClientHelper;

import java.util.concurrent.TimeUnit;

public class UpdateChecker implements Runnable {

    private final String agentId;
    private final String serverUrl;

    public UpdateChecker(String agentId, String serverUrl) {
        this.agentId = agentId;
        this.serverUrl = serverUrl;
    }

    @Override
    public void run() {
        while (true) {
            try {
                // Endpoint to check for updates
                String updateCheckUrl = serverUrl + "/agents/" + agentId + "/check-update";
                String response = HttpClientHelper.sendGetRequest(updateCheckUrl);

                // Assuming the response might indicate an update
                if (response.contains("updateAvailable")) {
                    System.out.println("Update available for " + agentId + ". Proceeding with update...");

                    // Logic to simulate an update
                    applyUpdate();
                }

                // Wait before the next check (e.g., every 60 seconds)
                TimeUnit.SECONDS.sleep(60);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void applyUpdate() {
        // Simulate the update process (e.g., print a message to indicate that an update is being applied)
        System.out.println("Applying firmware update to device " + agentId + "...");
    }
}
