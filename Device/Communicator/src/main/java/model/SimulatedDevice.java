package model;

/**
 * Represents a simulated IoT device in the fleet management system.
 * <p>
 * This class stores information such as the device ID, secret key, and the JWT token assigned to the device.
 * </p>
 *
 * @author Lara
 * @author Jasmin1707
 */
public class SimulatedDevice {
    private final String deviceId;
    private final String secretKey;
    private String jwtToken;

    /**
     * Constructor to create a simulated device with a device ID and secret key.
     *
     * @param deviceId  the unique identifier of the simulated device
     * @param secretKey the secret key of the simulated device
     */
    public SimulatedDevice(String deviceId, String secretKey) {
        this.deviceId = deviceId;
        this.secretKey = secretKey;
    }

    /**
     * Retrieves the unique identifier of the device.
     *
     * @return the device ID
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Retrieves the secret key of the device.
     *
     * @return the secret key
     */
    public String getSecretKey() {
        return secretKey;
    }

    /**
     * Retrieves the JWT token assigned to the device.
     *
     * @return the JWT token
     */
    public String getJwtToken() {
        return jwtToken;
    }

    /**
     * Sets the JWT token for the device.
     *
     * @param jwtToken the new JWT token
     */
    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }
}
