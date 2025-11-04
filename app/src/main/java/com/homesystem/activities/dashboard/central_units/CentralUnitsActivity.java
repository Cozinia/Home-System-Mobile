package com.homesystem.activities.dashboard.central_units;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.homesystem.R;
import com.homesystem.activities.dashboard.DashboardActivity;
import com.homesystem.models.CentralUnit;
import com.homesystem.models.Sensor;
import com.homesystem.models.User;
import com.homesystem.utils.SessionManager;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CentralUnitsActivity extends AppCompatActivity {
    private static final String TAG = "CentralUnitsDemo";
    private static final String MQTT_BROKER = "ssl://7bbc566c77ad48bdbf77ae86da3241a9.s1.eu.hivemq.cloud:8883";
    private static final String MQTT_USERNAME = "Crown2436";
    private static final String MQTT_PASSWORD = "PfKEsc#xQlsC8g^2B3nLzn";
    private static final String DEMO_DEVICE_ID = "D48AFCA33980";
    private static final String DEMO_USER_ID = "-OT67xVtTGXg7ppoiyhS";

    private MqttClient mqttClient;
    private boolean mqttConnected = false;
    private SessionManager sessionManager;
    private User currentUser;
    private List<CentralUnit> centralUnits = new ArrayList<>();
    private TextView tvUnitName, tvStatus;
    private Button btnArmDisarm;
    private boolean isDemoArmed = false;
    private Handler demoHandler = new Handler(Looper.getMainLooper());
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_central_units);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            finish();
            return;
        }

        currentUser = sessionManager.getCurrentUser();
        initUI();
        createDemoDevice();
        initMQTT();
        startDemoUpdates();

        Log.d(TAG, "Central Units Activity Started");
    }

    private void initUI() {
        tvUnitName = findViewById(R.id.tv_unit_name);
        tvStatus = findViewById(R.id.tv_status);
        btnArmDisarm = findViewById(R.id.btn_arm_disarm);

        if (btnArmDisarm != null) {
            btnArmDisarm.setOnClickListener(v -> toggleDevice());
        }

        View backBtn = findViewById(R.id.btn_back);
        if (backBtn != null) backBtn.setOnClickListener(v -> backToDashboard());

        View homeTab = findViewById(R.id.tab_home);
        if (homeTab != null) homeTab.setOnClickListener(v -> backToDashboard());
    }

    private void createDemoDevice() {
        CentralUnit demoUnit = new CentralUnit(
                DEMO_DEVICE_ID,
                "central_unit",
                "1.0.0-DEMO",
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                "online",
                DEMO_USER_ID
        );

        List<Sensor> demoSensors = new ArrayList<>();
        Sensor pirSensor = new Sensor(
                "7",                  // sensorId
                DEMO_DEVICE_ID,       // centralUnitId
                0,                    // sensorType
                false,                // sensorValue
                92,                   // batteryLevel
                80,                   // signalLevel
                System.currentTimeMillis(), // lastUpdate
                DEMO_USER_ID,         // userId
                "Living Room",        // location
                "PIR Motion Sensor"   // sensorName
        );

        Sensor reedSensor = new Sensor(
                "2",
                DEMO_DEVICE_ID,
                1,
                false,
                87,
                75,
                System.currentTimeMillis(),
                DEMO_USER_ID,
                "Front Door",
                "Reed Door Sensor"
        );

        demoSensors.add(pirSensor);
        demoSensors.add(reedSensor);

        demoUnit.setSensors(demoSensors);
        centralUnits.add(demoUnit);

        sessionManager.saveCentralUnits(centralUnits);
        sessionManager.saveSensors(demoSensors);

        updateUI();

        Log.d(TAG, "Demo device created with 2 sensors");
    }

    private void updateUI() {
        if (!centralUnits.isEmpty()) {
            CentralUnit unit = centralUnits.get(0);
            tvUnitName.setText("SafeHome Central Unit (DEMO)");
            String status = isDemoArmed ? "ARMED" : "DISARMED";
            tvStatus.setText("Status: " + status);
            tvStatus.setTextColor(isDemoArmed ? 0xFFFF5722 : 0xFF4CAF50);
            btnArmDisarm.setText(isDemoArmed ? "DISARM SYSTEM" : "ARM SYSTEM");
            btnArmDisarm.setBackgroundColor(isDemoArmed ? 0xFFFF5722 : 0xFF4CAF50);
            btnArmDisarm.setTextColor(0xFFFFFFFF);
            btnArmDisarm.setEnabled(true);

            Log.d(TAG, "UI Updated: " + status);
        }
    }

    private void backToDashboard() {
        startActivity(new Intent(this, DashboardActivity.class));
        finish();
    }

    private void initMQTT() {
        new Thread(() -> {
            try {
                String clientId = "AndroidDemo_" + System.currentTimeMillis();
                MemoryPersistence persistence = new MemoryPersistence();
                mqttClient = new MqttClient(MQTT_BROKER, clientId, persistence);

                MqttConnectOptions connOpts = new MqttConnectOptions();
                connOpts.setUserName(MQTT_USERNAME);
                connOpts.setPassword(MQTT_PASSWORD.toCharArray());
                connOpts.setCleanSession(true);
                connOpts.setConnectionTimeout(10);
                connOpts.setKeepAliveInterval(20);

                mqttClient.setCallback(new MqttCallbackExtended() {
                    @Override
                    public void connectComplete(boolean reconnect, String serverURI) {
                        Log.d(TAG, "MQTT Connected");
                        mqttConnected = true;
                        subscribeToTopics();
                    }

                    @Override
                    public void connectionLost(Throwable cause) {
                        Log.e(TAG, "MQTT Connection lost: " + cause.getMessage());
                        mqttConnected = false;
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) {
                        String payload = new String(message.getPayload());
                        Log.d(TAG, "MQTT Message: " + topic + " -> " + payload);
                        runOnUiThread(() -> handleMqttMessage(topic, payload));
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                        Log.d(TAG, "Message delivered");
                    }
                });

                mqttClient.connect(connOpts);
                Log.d(TAG, "Connecting to MQTT");

            } catch (MqttException e) {
                Log.e(TAG, "MQTT Error: " + e.getMessage());
            }
        }).start();
    }

    private void subscribeToTopics() {
        try {
            mqttClient.subscribe("esp32/set/data/" + DEMO_DEVICE_ID, 1);
            mqttClient.subscribe("esp32/crt/data", 1);
            mqttClient.subscribe("esp32/crt/heartbeat", 1);
            mqttClient.subscribe("esp32/crt/all_sensors", 1);
            Log.d(TAG, "Subscribed to all topics");
        } catch (MqttException e) {
            Log.e(TAG, "Subscription error: " + e.getMessage());
        }
    }

    private void handleMqttMessage(String topic, String payload) {
        try {
            if (topic.equals("esp32/crt/data")) {
                handleAlert(payload);
            } else if (topic.contains("esp32/set/data")) {
                handleCommandResponse(payload);
            } else if (topic.equals("esp32/crt/heartbeat")) {
                handleHeartbeat(payload);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling message: " + e.getMessage());
        }
    }

    private void handleAlert(String payload) {
        try {
            JSONObject json = new JSONObject(payload);
            String deviceId = json.optString("device_id", "");
            updateSensorStatus(deviceId, true);
            demoHandler.postDelayed(() -> updateSensorStatus(deviceId, false), 3000);
            Log.d(TAG, "Alert processed: " + payload);
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing alert: " + e.getMessage());
        }
    }

    private void handleCommandResponse(String payload) {
        try {
            JSONObject json = new JSONObject(payload);
            String status = json.optString("status", "");
            boolean armed = json.optBoolean("armed", isDemoArmed); // Fallback to current state if not provided

            Log.d(TAG, "Command response received: status=" + status + ", armed=" + armed);

            if ("success".equals(status)) {
                isDemoArmed = armed;
                runOnUiThread(this::updateUI);
                Log.d(TAG, "Command response: Success, armed state updated to " + isDemoArmed);
            } else {
                // Revert to previous state if command fails
                runOnUiThread(this::updateUI);
                Log.e(TAG, "Command failed: " + json.optString("message", "Unknown error"));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing command response: " + e.getMessage());
            runOnUiThread(this::updateUI); // Ensure UI is updated even on error
        }
    }

    private void handleHeartbeat(String payload) {
        try {
            JSONObject json = new JSONObject(payload);
            boolean armed = json.optBoolean("armed", false);

            if (isDemoArmed != armed) {
                isDemoArmed = armed;
                runOnUiThread(this::updateUI);
                Log.d(TAG, "Heartbeat sync: Armed = " + armed);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing heartbeat: " + e.getMessage());
        }
    }

    private void updateSensorStatus(String sensorId, boolean triggered) {
        if (!centralUnits.isEmpty()) {
            CentralUnit unit = centralUnits.get(0);
            for (Sensor sensor : unit.getSensors()) {
                if (sensor.getSensorId().equals(sensorId)) {
                    sensor.setSensorValue(triggered);
                    sensor.setLastUpdate(System.currentTimeMillis());
                    break;
                }
            }
            sessionManager.saveCentralUnits(centralUnits);
        }
    }

    private void toggleDevice() {
        if (!mqttConnected) {
            Log.e(TAG, "Not connected to device");
            return;
        }

        new Thread(() -> {
            try {
                boolean newArmedState = !isDemoArmed;
                String command = newArmedState ? "ARM" : "DISARM";

                JSONObject payload = new JSONObject();
                payload.put("command", command);
                payload.put("deviceId", DEMO_DEVICE_ID);
                payload.put("userId", DEMO_USER_ID);
                payload.put("timestamp", System.currentTimeMillis());
                payload.put("requestedState", newArmedState);

                String payloadString = payload.toString();
                Log.d(TAG, "Sending command: " + payloadString);

                MqttMessage message = new MqttMessage(payloadString.getBytes());
                message.setQos(1);
                mqttClient.publish("esp32/crt/data", message);

                // Optimistic UI update
                runOnUiThread(() -> {
                    isDemoArmed = newArmedState; // Temporarily update state
                    btnArmDisarm.setEnabled(false);
                    btnArmDisarm.setText("Sending...");
                    updateUI();
                    demoHandler.postDelayed(() -> {
                        btnArmDisarm.setEnabled(true);
                        updateUI();
                    }, 2000);
                });

            } catch (JSONException | MqttException e) {
                Log.e(TAG, "Error sending command: " + e.getMessage());
                runOnUiThread(() -> {
                    btnArmDisarm.setEnabled(true);
                    updateUI();
                });
            }
        }).start();
    }

    private void startDemoUpdates() {
        Runnable demoUpdater = new Runnable() {
            @Override
            public void run() {
                if (mqttConnected) {
                    simulateRandomSensorUpdate();
                }
                int delay = 30000 + random.nextInt(60000);
                demoHandler.postDelayed(this, delay);
            }
        };
        demoHandler.postDelayed(demoUpdater, 10000);
        Log.d(TAG, "Demo updates started");
    }

    private void simulateRandomSensorUpdate() {
        try {
            boolean isPIR = random.nextBoolean();
            JSONObject alert = new JSONObject();
            if (isPIR) {
                alert.put("alert_type", "motion_detected");
                alert.put("device_id", "7");
                alert.put("location", "Living Room");
            } else {
                alert.put("alert_type", "door_opened");
                alert.put("device_id", "2");
                alert.put("location", "Front Door");
            }
            alert.put("central_unit", DEMO_DEVICE_ID);
            alert.put("timestamp", System.currentTimeMillis());
            alert.put("battery", 85 + random.nextInt(15));
            alert.put("signal", -50 + random.nextInt(20));
            alert.put("armed", isDemoArmed);

            String payload = alert.toString();
            Log.d(TAG, "Simulating sensor alert: " + payload);
            handleAlert(payload);
        } catch (JSONException e) {
            Log.e(TAG, "Error simulating sensor update: " + e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mqttClient != null && !mqttConnected) {
            initMQTT();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (demoHandler != null) {
            demoHandler.removeCallbacksAndMessages(null);
        }
        try {
            if (mqttClient != null && mqttConnected) {
                mqttClient.disconnect();
                mqttConnected = false;
            }
        } catch (MqttException e) {
            Log.e(TAG, "MQTT Disconnect error: " + e.getMessage());
        }
        Log.d(TAG, "Demo activity destroyed");
    }
}