package IoTFleetManagement.device.simulation;

import IoTFleetManagement.device.utils.HttpClientHelper;

public class HeartbeatSender implements Runnable {

    private final String agentId;
    private final String serverUrl;

    public HeartbeatSender(String agentId, String serverUrl) {
        this.agentId = agentId;
        this.serverUrl = serverUrl;
    }

    @Override
    public void run() {
        while (true) {
            try {
                String url = serverUrl + "/agents/" + agentId + "/ping";
                HttpClientHelper.sendGetRequest(url);
                Thread.sleep(5000); // Alle 5 Sekunden senden
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
