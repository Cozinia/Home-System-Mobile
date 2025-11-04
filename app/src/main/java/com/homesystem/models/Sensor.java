package com.homesystem.models;

import java.util.HashMap;
import java.util.Map;

public class Sensor {
    private String sensorId;
    private String centralUnitId;
    private int sensorType;
    private boolean sensorValue;
    private int batteryLevel;
    private int signalLevel;
    private long lastUpdate;
    private String userId;
    private String location;
    private String sensorName;

    public Sensor(String sensorId, String centralUnitId, int sensorType, boolean sensorValue,
                  int batteryLevel, int signalLevel, long lastUpdate, String userId,
                  String location, String sensorName) {
        this.sensorId = sensorId;
        this.centralUnitId = centralUnitId;
        this.sensorType = sensorType;
        this.sensorValue = sensorValue;
        this.batteryLevel = batteryLevel;
        this.signalLevel = signalLevel;
        this.lastUpdate = lastUpdate;
        this.userId = userId;
        this.location = location;
        this.sensorName = sensorName;
    }

    public static Sensor fromMap(Map<String, Object> data) {
        return new Sensor(
                (String) data.get("sensorId"),
                (String) data.get("centralUnitId"),
                ((Long) data.getOrDefault("sensorType", 0L)).intValue(),
                (Boolean) data.getOrDefault("sensorValue", false),
                ((Long) data.getOrDefault("batteryLevel", 100L)).intValue(),
                ((Long) data.getOrDefault("signalLevel", -100L)).intValue(),
                (Long) data.getOrDefault("lastUpdate", System.currentTimeMillis()),
                (String) data.get("userId"),
                (String) data.getOrDefault("location", "Unknown"),
                (String) data.getOrDefault("sensorName", "Unknown Sensor")
        );
    }



    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("sensorId", sensorId);
        map.put("centralUnitId", centralUnitId);
        map.put("sensorType", sensorType);
        map.put("sensorValue", sensorValue);
        map.put("batteryLevel", batteryLevel);
        map.put("signalLevel", signalLevel);
        map.put("lastUpdate", lastUpdate);
        map.put("userId", userId);
        map.put("location", location);
        map.put("sensorName", sensorName);
        return map;
    }

    // Getters and setters
    public String getSensorId() { return sensorId; }
    public String getCentralUnitId() { return centralUnitId; }
    public int getSensorType() { return sensorType; }
    public boolean getSensorValue() { return sensorValue; }
    public int getBatteryLevel() { return batteryLevel; }
    public int getSignalLevel() { return signalLevel; }
    public long getLastUpdate() { return lastUpdate; }
    public String getUserId() { return userId; }
    public String getLocation() { return location; }
    public String getSensorName() { return sensorName; }

    public void setSensorValue(boolean sensorValue) { this.sensorValue = sensorValue; }
    public void setBatteryLevel(int batteryLevel) { this.batteryLevel = batteryLevel; }
    public void setSignalLevel(int signalLevel) { this.signalLevel = signalLevel; }
    public void setLastUpdate(long lastUpdate) { this.lastUpdate = lastUpdate; }
    public void setLocation(String location) { this.location = location; }
    public void setSensorName(String sensorName) { this.sensorName = sensorName; }

    public String getSensorValueText() {
        if (sensorType == 0) { // PIR Sensor
            return sensorValue ? "MOTION DETECTED" : "NORMAL";
        } else { // Reed Sensor
            return sensorValue ? "OPENED" : "CLOSED";
        }
    }
}