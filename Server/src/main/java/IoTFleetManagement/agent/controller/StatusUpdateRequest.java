package IoTFleetManagement.agent.controller;

/**
 * Request class for updating the status of an IoT agent.
 * <p>
 * This class is used to encapsulate the information needed to update the status of an agent,
 * specifically whether the agent is online or not.
 *
 * @author Lara
 * @author Jasmin1707
 */
public class StatusUpdateRequest {
    private boolean online;

    /**
     * Returns whether the agent is online.
     *
     * @return {@code true} if the agent is online, {@code false} otherwise
     */
    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
  }
}
