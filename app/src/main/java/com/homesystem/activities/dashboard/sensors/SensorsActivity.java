package com.homesystem.activities.dashboard.sensors;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.homesystem.R;
import com.homesystem.activities.dashboard.DashboardActivity;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SensorsActivity extends AppCompatActivity {
    private static final String TAG = "SensorsLog";
    private static final String MQTT_BROKER = "ssl://7bbc566c77ad48bdbf77ae86da3241a9.s1.eu.hivemq.cloud:8883";
    private static final String MQTT_USERNAME = "Crown2436";
    private static final String MQTT_PASSWORD = "PfKEsc#xQlsC8g^2B3nLzn";
    private static final String SENSOR_TOPIC = "esp32/crt/all_sensors";
    private static final String ALERT_TOPIC = "esp32/crt/data";

    private MqttClient mqttClient;
    private boolean mqttConnected = false;
    private SessionManager sessionManager;
    private User currentUser;
    private List<Sensor> sensors = new ArrayList<>();
    private LinearLayout sensorsContainer;
    private LinearLayout emptyStateLayout;
    private FirebaseFirestore db;

    private Handler motionResetHandler = new Handler(Looper.getMainLooper());
    private Runnable motionResetRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors);

        sessionManager = new SessionManager(this);
        db = FirebaseFirestore.getInstance();
        if (!sessionManager.isLoggedIn()) {
            finish();
            return;
        }

        currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "Current user is null");
            finish();
            return;
        }

        sensors = sessionManager.getSensors();
        initUI();
        loadSensorsFromFirestore();
        new Handler(Looper.getMainLooper()).postDelayed(this::initMQTT, 2000);
    }

    private void initUI() {
        View backBtn = findViewById(R.id.btn_back);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> backToDashboard());
        }

        View refreshBtn = findViewById(R.id.btn_refresh);
        if (refreshBtn != null) {
            refreshBtn.setOnClickListener(v -> requestSensorUpdate());
        }

        View homeTab = findViewById(R.id.tab_home);
        if (homeTab != null) {
            homeTab.setOnClickListener(v -> backToDashboard());
        }

        sensorsContainer = findViewById(R.id.sensors_container);
        emptyStateLayout = findViewById(R.id.empty_state_layout);
    }

    private void backToDashboard() {
        startActivity(new Intent(this, DashboardActivity.class));
        finish();
    }

    private void loadSensorsFromFirestore() {
        if (currentUser != null) {
            List<Sensor> allSensors = new ArrayList<>();
            db.collection("central_units")
                    .whereEqualTo("userId", currentUser.getId())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (var unitDoc : queryDocumentSnapshots) {
                            String unitId = unitDoc.getId();
                            db.collection("central_units").document(unitId).collection("sensors")
                                    .get()
                                    .addOnSuccessListener(sensorDocs -> {
                                        for (var sensorDoc : sensorDocs) {
                                            Sensor sensor = Sensor.fromMap(sensorDoc.getData());
                                            allSensors.add(sensor);
                                        }
                                        sensors = allSensors;
                                        sessionManager.saveSensors(sensors);
                                        runOnUiThread(this::updateUI);
                                    })
                                    .addOnFailureListener(e -> Log.e(TAG, "Error loading sensors: " + e.getMessage()));
                        }
                        if (allSensors.isEmpty()) {
                            createTestSensor();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading central units: " + e.getMessage());
                        createTestSensor();
                    });
        }
    }

    private void createTestSensor() {
        if (currentUser != null) {
            Sensor testSensor = new Sensor(
                    "SENSOR-D48AFCA33980-1",
                    "D48AFCA33980",
                    0,
                    false,
                    90,
                    -80,
                    System.currentTimeMillis(),
                    currentUser.getId(),
                    "Living Room",
                    "Test PIR Sensor"
            );
            sensors.add(testSensor);
            sessionManager.saveSensors(sensors);
            db.collection("central_units").document("D48AFCA33980")
                    .collection("sensors").document(testSensor.getSensorId())
                    .set(testSensor.toMap())
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Test sensor saved: " + testSensor.getSensorId()))
                    .addOnFailureListener(e -> Log.e(TAG, "Error saving test sensor: " + e.getMessage()));
            runOnUiThread(this::updateUI);
        }
    }

    private void updatePirSensorUI(Sensor sensor) {
        TextView tvStatus = findViewById(R.id.tv_pir_status);
        TextView tvBattery = findViewById(R.id.tv_pir_battery);
        TextView tvId = findViewById(R.id.tv_pir_id);
        TextView tvSignal = findViewById(R.id.tv_pir_signal);
        TextView tvLocation = findViewById(R.id.tv_pir_location);
        TextView tvName = findViewById(R.id.tv_pir_name);
        FrameLayout iconContainer = findViewById(R.id.pir_icon_container);

        if (tvStatus != null) {
            tvStatus.setText(sensor.getSensorValue() ? "MOTION DETECTED" : "ALL GOOD");
            tvStatus.setBackgroundResource(sensor.getSensorValue() ?
                    R.drawable.status_background_alert : R.drawable.status_background_normal);
            tvStatus.setTextColor(getResources().getColor(sensor.getSensorValue() ?
                    android.R.color.white : android.R.color.black));
        }
        if (tvBattery != null) tvBattery.setText(sensor.getBatteryLevel() + "%");
        if (tvId != null) tvId.setText(sensor.getSensorId());
        if (tvSignal != null) tvSignal.setText(sensor.getSignalLevel() + " dBm");
        if (tvLocation != null) tvLocation.setText(sensor.getLocation());
        if (tvName != null) tvName.setText(sensor.getSensorName());
        if (iconContainer != null) {
            iconContainer.setBackgroundResource(sensor.getSensorValue() ?
                    R.drawable.circle_red : R.drawable.circle_green);
        }
    }

    private void updateReedSensorUI(Sensor sensor) {
        TextView tvStatus = findViewById(R.id.tv_reed_status);
        TextView tvBattery = findViewById(R.id.tv_reed_battery);
        TextView tvId = findViewById(R.id.tv_reed_id);
        TextView tvSignal = findViewById(R.id.tv_reed_signal);
        TextView tvLocation = findViewById(R.id.tv_reed_location);
        TextView tvName = findViewById(R.id.tv_reed_name);
        FrameLayout iconContainer = findViewById(R.id.reed_icon_container);

        if (tvStatus != null) {
            tvStatus.setText(sensor.getSensorValue() ? "OPENED" : "CLOSED");
            tvStatus.setBackgroundResource(sensor.getSensorValue() ?
                    R.drawable.status_background_alert : R.drawable.status_background_normal);
            tvStatus.setTextColor(getResources().getColor(sensor.getSensorValue() ?
                    android.R.color.white : android.R.color.black));
        }
        if (tvBattery != null) tvBattery.setText(sensor.getBatteryLevel() + "%");
        if (tvId != null) tvId.setText(sensor.getSensorId());
        if (tvSignal != null) tvSignal.setText(sensor.getSignalLevel() + " dBm");
        if (tvLocation != null) tvLocation.setText(sensor.getLocation());
        if (tvName != null) tvName.setText(sensor.getSensorName());
        if (iconContainer != null) {
            iconContainer.setBackgroundResource(sensor.getSensorValue() ?
                    R.drawable.circle_red : R.drawable.circle_background_orange);
        }
    }

    private void updateUI() {
        if (sensors.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            sensorsContainer.setVisibility(View.GONE);
            return;
        }

        emptyStateLayout.setVisibility(View.GONE);
        sensorsContainer.setVisibility(View.VISIBLE);

        for (Sensor sensor : sensors) {
            if (sensor.getSensorType() == 0) {
                updatePirSensorUI(sensor);
            } else {
                updateReedSensorUI(sensor);
            }
        }
    }

    private void initMQTT() {
        new Thread(() -> {
            try {
                if (!checkInternetConnection()) {
                    Log.e(TAG, "No internet connection");
                    return;
                }

                String clientId = "AndroidApp_" + System.currentTimeMillis();
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
                        Log.d(TAG, "MQTT connected: " + serverURI);
                        mqttConnected = true;
                        subscribeToTopics();
                    }

                    @Override
                    public void connectionLost(Throwable cause) {
                        Log.e(TAG, "MQTT connection lost: " + (cause != null ? cause.getMessage() : "Unknown"));
                        mqttConnected = false;
                        runOnUiThread(() -> updateUI());
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) {
                        String payload = new String(message.getPayload());
                        Log.d(TAG, "Message received - Topic: " + topic + ", Payload: " + payload);
                        if (topic.equals(SENSOR_TOPIC)) {
                            handleAllSensorsData(payload);
                        } else if (topic.equals(ALERT_TOPIC)) {
                            handleAlertMessage(payload);
                        }
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                        Log.d(TAG, "Message delivery complete");
                    }
                });

                Log.d(TAG, "Connecting to MQTT broker: " + MQTT_BROKER);
                mqttClient.connect(connOpts);

            } catch (MqttException e) {
                Log.e(TAG, "MQTT error: " + e.getMessage() + ", Code: " + e.getReasonCode());
            }
        }).start();
    }

    private void subscribeToTopics() {
        try {
            mqttClient.subscribe(SENSOR_TOPIC, 0, (topic, message) -> {
                String payload = new String(message.getPayload());
                Log.d(TAG, "Sensor data - Topic: " + topic + ", Payload: " + payload);
                handleAllSensorsData(payload);
            });
            Log.d(TAG, "Subscribed to: " + SENSOR_TOPIC);

            mqttClient.subscribe(ALERT_TOPIC, 0, (topic, message) -> {
                String payload = new String(message.getPayload());
                Log.d(TAG, "Alert message - Topic: " + topic + ", Payload: " + payload);
                handleAlertMessage(payload);
            });
            Log.d(TAG, "Subscribed to: " + ALERT_TOPIC);

        } catch (MqttException e) {
            Log.e(TAG, "Subscription error: " + e.getMessage());
        }
    }

    private void handleAllSensorsData(String payload) {
        try {
            JSONObject json = new JSONObject(payload);
            String centralUnitId = json.optString("central_unit", "");
            JSONArray sensorsArray = json.optJSONArray("sensors");

            if (sensorsArray != null && !centralUnitId.isEmpty()) {
                List<Sensor> newSensors = new ArrayList<>();
                for (int i = 0; i < sensorsArray.length(); i++) {
                    JSONObject sensorJson = sensorsArray.getJSONObject(i);
                    Sensor sensor = new Sensor(
                            sensorJson.optString("id", "SENSOR-" + centralUnitId + "-" + i),
                            centralUnitId,
                            sensorJson.optInt("type", 0),
                            sensorJson.optBoolean("value", false),
                            sensorJson.optInt("battery", 100),
                            sensorJson.optInt("signal", -100),
                            System.currentTimeMillis(),
                            currentUser.getId(),
                            sensorJson.optString("location", "Unknown"),
                            sensorJson.optString("sensorName", "Unknown Sensor")
                    );
                    newSensors.add(sensor);
                    db.collection("central_units").document(centralUnitId)
                            .collection("sensors").document(sensor.getSensorId())
                            .set(sensor.toMap())
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Sensor saved: " + sensor.getSensorId()))
                            .addOnFailureListener(e -> Log.e(TAG, "Error saving sensor: " + e.getMessage()));
                }

                sensors = newSensors;
                sessionManager.saveSensors(sensors);
                runOnUiThread(this::updateUI);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing sensors data: " + e.getMessage());
        }
    }


    private void handleAlertMessage(String payload) {
        try {
            JSONObject json = new JSONObject(payload);
            String alertType = json.optString("alert_type", "");
            String sensorId = json.optString("device_id", "");
            String centralUnitId = json.optString("central_unit", "");
            int battery = json.optInt("battery", 100);
            int signal = json.optInt("signal", -100);
            String location = json.optString("location", "Unknown");
            String sensorName = json.optString("sensor_name", "Unknown Sensor");

            boolean sensorValue;
            if (json.has("motion")) {
                sensorValue = json.getInt("motion") == 1;
            } else {
                sensorValue = alertType.equals("motion_detected") || alertType.equals("door_opened");
            }

            boolean motionDetected = false;
            boolean isPirSensor = false;

            // Remove any pending reset if exists
            if (motionResetRunnable != null) {
                motionResetHandler.removeCallbacks(motionResetRunnable);
            }

            for (Sensor sensor : sensors) {
                if (sensor.getSensorId().equals(sensorId) && sensor.getCentralUnitId().equals(centralUnitId)) {
                    isPirSensor = sensor.getSensorType() == 0; // Check if it's a PIR sensor

                    if (isPirSensor && sensorValue) {
                        motionDetected = true;
                        // Schedule reset after 15 seconds
                        motionResetRunnable = () -> {
                            sensor.setSensorValue(false);
                            sessionManager.saveSensors(sensors);
                            runOnUiThread(() -> {
                                updateUI();
                                Toast.makeText(this, "Motion reset in " + location, Toast.LENGTH_SHORT).show();
                            });

                            // Update in Firestore
                            db.collection("central_units").document(centralUnitId)
                                    .collection("sensors").document(sensorId)
                                    .update("sensorValue", false)
                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Sensor reset: " + sensorId))
                                    .addOnFailureListener(e -> Log.e(TAG, "Error resetting sensor: " + e.getMessage()));
                        };
                        motionResetHandler.postDelayed(motionResetRunnable, 15000);
                    }

                    sensor.setSensorValue(sensorValue);
                    sensor.setBatteryLevel(battery);
                    sensor.setSignalLevel(signal);
                    sensor.setLocation(location);
                    sensor.setSensorName(sensorName);

                    db.collection("central_units").document(centralUnitId)
                            .collection("sensors").document(sensorId)
                            .update(
                                    "sensorValue", sensorValue,
                                    "batteryLevel", battery,
                                    "signalLevel", signal,
                                    "location", location,
                                    "sensorName", sensorName
                            )
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Sensor alert updated: " + sensorId))
                            .addOnFailureListener(e -> Log.e(TAG, "Error updating sensor alert: " + e.getMessage()));
                    break;
                }
            }

            sessionManager.saveSensors(sensors);
            boolean finalMotionDetected = motionDetected;
            runOnUiThread(() -> {
                updateUI();
                if (finalMotionDetected) {
                    Toast.makeText(this, "Motion detected in " + location, Toast.LENGTH_LONG).show();
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing alert message: " + e.getMessage());
        }
    }


    private void requestSensorUpdate() {
        new Thread(() -> {
            try {
                if (mqttClient != null && mqttConnected && !sensors.isEmpty()) {
                    for (Sensor sensor : sensors) {
                        JSONObject payload = new JSONObject();
                        payload.put("command", "SEND_DATA");
                        payload.put("deviceId", sensor.getCentralUnitId());
                        payload.put("userId", currentUser.getId());

                        String payloadString = payload.toString();
                        Log.d(TAG, "Requesting sensor update - Topic: " + ALERT_TOPIC + ", Payload: " + payloadString);

                        MqttMessage message = new MqttMessage(payloadString.getBytes());
                        message.setQos(1);
                        mqttClient.publish(ALERT_TOPIC, message);
                    }
                } else {
                    Log.e(TAG, "Cannot request update - MQTT not connected or no sensors");
                }
            } catch (JSONException | MqttException e) {
                Log.e(TAG, "Error: " + e.getMessage());
            }
        }).start();
    }

    private boolean checkInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mqttClient != null && !mqttConnected) {
            initMQTT();
        }
        loadSensorsFromFirestore();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up handler to prevent memory leaks
        if (motionResetHandler != null && motionResetRunnable != null) {
            motionResetHandler.removeCallbacks(motionResetRunnable);
        }
        try {
            if (mqttClient != null && mqttConnected) {
                mqttClient.disconnect();
                mqttConnected = false;
            }
        } catch (MqttException e) {
            Log.e(TAG, "Disconnect error: " + e.getMessage());
        }
    }
}