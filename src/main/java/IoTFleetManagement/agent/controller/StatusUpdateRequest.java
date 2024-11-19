package IoTFleetManagement.agent.controller;

public class StatusUpdateRequest {
    private boolean online;

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}
