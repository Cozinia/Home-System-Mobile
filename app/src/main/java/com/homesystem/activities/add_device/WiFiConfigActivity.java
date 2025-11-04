package com.homesystem.activities.add_device;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.homesystem.R;
import com.homesystem.utils.Validator;

public class WiFiConfigActivity extends AppCompatActivity {

    // Header views
    private ImageView btnBack;
    private TextView tvTitle, tvSubtitle, tvStepIndicator;

    // WiFi Config views
    private TextInputEditText etWifiSSID, etWifiPassword;
    private Button btnDetectWifi, btnNext;

    private String deviceType;
    private Handler handler = new Handler();
    private ActivityResultLauncher<String[]> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadSavedLanguage();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_config);

        // Get device type from previous activity
        deviceType = getIntent().getStringExtra("device_type");
        if (deviceType == null) deviceType = "central_unit";

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    if (result.values().stream().allMatch(Boolean::booleanValue)) {
                        autoDetectCurrentWifi();
                    } else {
                        Toast.makeText(this, "Permissions needed for WiFi auto-detection", Toast.LENGTH_LONG).show();
                    }
                });

        initializeViews();
        setupClickListeners();
    }

    private void loadSavedLanguage() {
        String savedLanguage = getSharedPreferences("AppSettings", MODE_PRIVATE)
                .getString("language", "en");
        // Apply language if needed
    }

    private void initializeViews() {
        // Header views
        btnBack = findViewById(R.id.wifi_config_btnBack);
        tvTitle = findViewById(R.id.wifi_config_tvTitle);
        tvSubtitle = findViewById(R.id.wifi_config_tvSubtitle);
        tvStepIndicator = findViewById(R.id.wifi_config_tvStepIndicator);

        // WiFi Config views
        etWifiSSID = findViewById(R.id.wifi_config_etWifiSSID);
        etWifiPassword = findViewById(R.id.wifi_config_etWifiPassword);
        btnDetectWifi = findViewById(R.id.wifi_config_btnDetectWifi);
        btnNext = findViewById(R.id.wifi_config_btnNext);

        // Set initial values
        tvTitle.setText(R.string.add_new_device);
        tvSubtitle.setText("Configure WiFi credentials for your device");
        tvStepIndicator.setText("Step 1 of 3");
    }

    private void setupClickListeners() {
        // Back button
        btnBack.setOnClickListener(v -> onBackPressed());

        // Auto-detect WiFi button
        btnDetectWifi.setOnClickListener(v -> {
            btnDetectWifi.setEnabled(false);
            btnDetectWifi.setText("Detecting...");
            handler.postDelayed(() -> {
                checkPermissionsAndDetectWifi();
                btnDetectWifi.setText("Auto Detect WiFi");
                btnDetectWifi.setEnabled(true);
            }, 500);
        });

        // Next button
        btnNext.setOnClickListener(v -> {
            if (validateWifiCredentials()) {
                // Pass WiFi credentials and device type to next activity
                Intent intent = new Intent(this, DeviceConnectActivity.class);
                intent.putExtra("wifi_ssid", etWifiSSID.getText().toString().trim());
                intent.putExtra("wifi_password", etWifiPassword.getText().toString().trim());
                intent.putExtra("device_type", deviceType);
                startActivity(intent);
            }
        });
    }

    private boolean validateWifiCredentials() {
        String ssid = etWifiSSID.getText().toString().trim();
        String password = etWifiPassword.getText().toString().trim();
        boolean isValid = true;

        if (Validator.checkNullOrEmpty(ssid)) {
            etWifiSSID.setError("WiFi network name is required");
            etWifiSSID.requestFocus();
            isValid = false;
        }

        if (Validator.checkNullOrEmpty(password)) {
            etWifiPassword.setError("WiFi password is required");
            if (isValid) etWifiPassword.requestFocus();
            isValid = false;
        } else if (password.length() < 8) {
            etWifiPassword.setError("WiFi password must be at least 8 characters");
            if (isValid) etWifiPassword.requestFocus();
            isValid = false;
        }

        return isValid;
    }

    private void checkPermissionsAndDetectWifi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
                    new String[]{Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.ACCESS_NETWORK_STATE,
                            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES} :
                    new String[]{Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.ACCESS_NETWORK_STATE,
                            Manifest.permission.ACCESS_FINE_LOCATION};

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {
                autoDetectCurrentWifi();
            } else {
                requestPermissionLauncher.launch(permissions);
            }
        } else {
            autoDetectCurrentWifi();
        }
    }

    private void autoDetectCurrentWifi() {
        try {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            if (wifiManager == null || !wifiManager.isWifiEnabled()) {
                Toast.makeText(this, "Please enable WiFi to auto-detect network", Toast.LENGTH_SHORT).show();
                return;
            }

            android.net.wifi.WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                String currentSSID = wifiInfo.getSSID();
                if (currentSSID != null && !currentSSID.equals("<unknown ssid>") && !currentSSID.equals("\"\"")) {
                    currentSSID = currentSSID.replace("\"", "");
                    etWifiSSID.setText(currentSSID);
                    Toast.makeText(this, "Auto-detected: " + currentSSID, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Could not auto-detect WiFi network", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (SecurityException e) {
            Toast.makeText(this, "Permission needed to detect WiFi network", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error detecting WiFi network", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}