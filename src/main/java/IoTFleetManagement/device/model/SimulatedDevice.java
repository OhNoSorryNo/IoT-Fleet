package IoTFleetManagement.device.model;


public class SimulatedDevice {
    private final String deviceId;
    private final String secretKey;
    private String jwtToken;

    // Constructors, Getters, and Setters
    public SimulatedDevice(String deviceId, String secretKey) {
        this.deviceId = deviceId;
        this.secretKey = secretKey;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }
}
