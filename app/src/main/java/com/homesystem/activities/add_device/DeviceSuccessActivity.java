package com.homesystem.activities.add_device;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.homesystem.R;
import com.homesystem.activities.dashboard.DashboardActivity;
import com.homesystem.activities.dashboard.central_units.CentralUnitsActivity;
import com.homesystem.models.CentralUnit;
import com.homesystem.utils.SessionManager;

public class DeviceSuccessActivity extends AppCompatActivity {
    private static final String TAG = "DeviceSuccessActivity";

    // Header views
    private ImageView btnBack;
    private TextView tvTitle, tvSubtitle, tvStepIndicator;

    // Success views
    private TextView tvDeviceName, tvWifiNetwork;
    private Button btnFinish;

    private String wifiSSID, wifiPassword, deviceType, deviceNetwork, devicePassword, deviceId;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_success);

        // Get data from previous activity
        Intent intent = getIntent();
        wifiSSID = intent.getStringExtra("wifi_ssid");
        wifiPassword = intent.getStringExtra("wifi_password");
        deviceType = intent.getStringExtra("device_type");
        deviceNetwork = intent.getStringExtra("device_network");
        devicePassword = intent.getStringExtra("device_password");
        deviceId = intent.getStringExtra("device_id");

        sessionManager = new SessionManager(this);

        initializeViews();
        setupClickListeners();
        displayDeviceInfo();

        // Save the new device to preferences
        saveNewDevice();
    }

    private void initializeViews() {
        // Header views
        btnBack = findViewById(R.id.device_success_btnBack);
        tvTitle = findViewById(R.id.device_success_tvTitle);
        tvSubtitle = findViewById(R.id.device_success_tvSubtitle);
        tvStepIndicator = findViewById(R.id.device_success_tvStepIndicator);

        // Success views
        tvDeviceName = findViewById(R.id.device_success_tvDeviceName);
        tvWifiNetwork = findViewById(R.id.device_success_tvWifiNetwork);
        btnFinish = findViewById(R.id.device_success_btnFinish);

        // Set header text
        tvTitle.setText("Dispozitiv Adăugat!");
        tvSubtitle.setText("Dispozitivul a fost configurat cu succes și adăugat în sistem");
        tvStepIndicator.setText("Complet");
    }

    private void setupClickListeners() {
        // Back button - go to central units
        btnBack.setOnClickListener(v -> navigateToCentralUnits());

        // Main finish button - go directly to central units
        btnFinish.setOnClickListener(v -> navigateToCentralUnits());
    }

    private void showFinishOptions() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Ce dorești să faci acum?")
                .setItems(new CharSequence[]{
                        "Vezi Unitățile Centrale",
                        "Mergi la Dashboard",
                        "Adaugă Alt Dispozitiv"
                }, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            navigateToCentralUnits();
                            break;
                        case 1:
                            navigateToDashboard();
                            break;
                        case 2:
                            addAnotherDevice();
                            break;
                    }
                })
                .setNegativeButton("Anulează", null)
                .show();
    }

    private void saveNewDevice() {
        try {
            // Save device info to SharedPreferences for demonstration
            SharedPreferences prefs = getSharedPreferences("SavedDevices", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            String userId = sessionManager.getCurrentUserId();
            if (userId != null) {
                // Create a unique key for this device
                String deviceKey = "device_" + (deviceId != null ? deviceId : System.currentTimeMillis());

                // Save device data as JSON-like string
                String deviceData = String.format(
                        "{\"id\":\"%s\",\"name\":\"%s\",\"type\":\"%s\",\"wifi\":\"%s\",\"userId\":\"%s\",\"addedAt\":%d}",
                        deviceId != null ? deviceId : "MCU-" + System.currentTimeMillis(),
                        deviceNetwork != null ? deviceNetwork : "SafeHome Device",
                        deviceType != null ? deviceType : "central_unit",
                        wifiSSID != null ? wifiSSID : "Unknown WiFi",
                        userId,
                        System.currentTimeMillis()
                );

                editor.putString(deviceKey, deviceData);
                editor.putBoolean("has_new_device", true);
                editor.apply();

                Log.d(TAG, "Device saved: " + deviceKey + " = " + deviceData);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving device: " + e.getMessage());
        }
    }

    private void displayDeviceInfo() {
        // Display device network name
        if (deviceNetwork != null && !deviceNetwork.isEmpty()) {
            tvDeviceName.setText(deviceNetwork);
        } else {
            tvDeviceName.setText("SafeHome Device");
        }

        // Display WiFi network
        if (wifiSSID != null && !wifiSSID.isEmpty()) {
            tvWifiNetwork.setText("Conectat la: " + wifiSSID);
        } else {
            tvWifiNetwork.setText("WiFi Conectat");
        }
    }

    private void navigateToCentralUnits() {
        try {
            Log.d(TAG, "Navigating to Central Units activity");
            Intent intent = new Intent(this, CentralUnitsActivity.class);

            // Pass device info to show the new device
            intent.putExtra("new_device_added", true);
            intent.putExtra("device_network", deviceNetwork);
            intent.putExtra("device_type", deviceType);
            intent.putExtra("device_id", deviceId);
            intent.putExtra("from_setup", true);

            // Clear previous activities and start Central Units
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

        } catch (Exception e) {
            Log.e(TAG, "Error navigating to Central Units: " + e.getMessage());
            // Fallback to dashboard
            navigateToDashboard();
        }
    }

    private void navigateToDashboard() {
        try {
            Log.d(TAG, "Navigating to Dashboard");
            Intent intent = new Intent(this, DashboardActivity.class);

            // Pass device info to dashboard
            intent.putExtra("new_device_added", true);
            intent.putExtra("device_network", deviceNetwork);
            intent.putExtra("device_type", deviceType);
            intent.putExtra("show_success_message", true);

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

        } catch (Exception e) {
            Log.e(TAG, "Error navigating to Dashboard: " + e.getMessage());
            finishAffinity();
        }
    }

    private void addAnotherDevice() {
        try {
            // Go back to device setup flow
            Intent intent = new Intent(this, com.homesystem.activities.add_device.WiFiConfigActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error starting device setup: " + e.getMessage());
            navigateToDashboard();
        }
    }

    @Override
    public void onBackPressed() {
        // Navigate to Central Units instead of Dashboard
        navigateToCentralUnits();
    }
}