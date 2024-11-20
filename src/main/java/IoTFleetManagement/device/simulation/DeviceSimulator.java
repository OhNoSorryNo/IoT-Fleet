package IoTFleetManagement.device.simulation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@SpringBootApplication
@Component
public class DeviceSimulator {

    @Value("${device.simulation.agentId}")
    private String agentId;

    @Value("${device.simulation.server.url}")
    private String serverUrl;

    @PostConstruct
    public void startSimulation() {
        // Start the heartbeat and update check processes
        HeartbeatSender heartbeatSender = new HeartbeatSender(agentId, serverUrl);
        UpdateChecker updateChecker = new UpdateChecker(agentId, serverUrl);

        new Thread(heartbeatSender).start();
        new Thread(updateChecker).start();
    }
}
