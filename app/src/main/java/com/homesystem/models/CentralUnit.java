package com.homesystem.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CentralUnit {
    private String deviceId;
    private String deviceType;
    private String firmware;
    private long lastHeartbeat;
    private long registeredAt;
    private String status;
    private String userId;
    private List<Sensor> sensors;

    // Default constructor required for Firebase
    public CentralUnit() {
        this.sensors = new ArrayList<>();
    }

    // Constructor
    public CentralUnit(String deviceId, String deviceType, String firmware, long lastHeartbeat,
                       long registeredAt, String status, String userId) {
        this.deviceId = deviceId;
        this.deviceType = deviceType;
        this.firmware = firmware;
        this.lastHeartbeat = lastHeartbeat;
        this.registeredAt = registeredAt;
        this.status = status;
        this.userId = userId;
        this.sensors = new ArrayList<>();
    }

    // Constructor with sensors
    public CentralUnit(String deviceId, String deviceType, String firmware, long lastHeartbeat,
                       long registeredAt, String status, String userId, List<Sensor> sensors) {
        this(deviceId, deviceType, firmware, lastHeartbeat, registeredAt, status, userId);
        this.sensors = sensors != null ? sensors : new ArrayList<>();
    }

    // Getters and Setters
    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getFirmware() {
        return firmware;
    }

    public void setFirmware(String firmware) {
        this.firmware = firmware;
    }

    public long getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(long lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public long getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(long registeredAt) {
        this.registeredAt = registeredAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<Sensor> getSensors() {
        return sensors;
    }

    public void setSensors(List<Sensor> sensors) {
        this.sensors = sensors != null ? sensors : new ArrayList<>();
    }

    // Helper methods
    public String getStatusText() {
        return "online".equals(status) ? "Online" : "Offline";
    }

    public int getStatusColor() {
        return "online".equals(status) ? android.R.color.holo_green_dark : android.R.color.holo_red_dark;
    }

    public String getFormattedLastHeartbeat() {
        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - lastHeartbeat;

        if (timeDiff < 60000) {
            return "Just now";
        } else if (timeDiff < 3600000) {
            int minutes = (int) (timeDiff / 60000);
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        } else if (timeDiff < 86400000) {
            int hours = (int) (timeDiff / 3600000);
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else {
            int days = (int) (timeDiff / 86400000);
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        }
    }

    // Convert to Map for Firebase
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("deviceId", deviceId);
        map.put("deviceType", deviceType);
        map.put("firmware", firmware);
        map.put("lastHeartbeat", lastHeartbeat);
        map.put("registeredAt", registeredAt);
        map.put("status", status);
        map.put("userId", userId);
        return map;
    }

    // Create from Map (Firebase)
    public static CentralUnit fromMap(Map<String, Object> map) {
        CentralUnit unit = new CentralUnit();
        unit.setDeviceId((String) map.get("deviceId"));
        unit.setDeviceType((String) map.get("deviceType"));
        unit.setFirmware((String) map.get("firmware"));
        Object lastHeartbeatObj = map.get("lastHeartbeat");
        if (lastHeartbeatObj instanceof Long) {
            unit.setLastHeartbeat((Long) lastHeartbeatObj);
        }
        Object registeredAtObj = map.get("registeredAt");
        if (registeredAtObj instanceof Long) {
            unit.setRegisteredAt((Long) registeredAtObj);
        }
        unit.setStatus((String) map.get("status"));
        unit.setUserId((String) map.get("userId"));
        unit.setSensors(new ArrayList<>());
        return unit;
    }

    @Override
    public String toString() {
        return "CentralUnit{" +
                "deviceId='" + deviceId + '\'' +
                ", deviceType='" + deviceType + '\'' +
                ", firmware='" + firmware + '\'' +
                ", lastHeartbeat=" + lastHeartbeat +
                ", registeredAt=" + registeredAt +
                ", status='" + status + '\'' +
                ", userId='" + userId + '\'' +
                ", sensors=" + sensors +
                '}';
    }
}
