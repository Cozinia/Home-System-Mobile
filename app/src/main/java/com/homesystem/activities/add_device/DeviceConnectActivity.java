package com.homesystem.activities.add_device;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.homesystem.R;

public class DeviceConnectActivity extends AppCompatActivity {
    private static final String TAG = "DeviceConnectActivity";

    // Header views
    private ImageView btnBack;
    private TextView tvTitle, tvSubtitle, tvStepIndicator;

    // AP Connect views
    private TextView tvNetworkName, tvNetworkPassword;
    private Button btnPrevious, btnNext, btnOpenWifiSettings;

    private String wifiSSID, wifiPassword, deviceType;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_connect);

        // Get WiFi credentials from previous activity
        Intent intent = getIntent();
        wifiSSID = intent.getStringExtra("wifi_ssid");
        wifiPassword = intent.getStringExtra("wifi_password");
        deviceType = intent.getStringExtra("device_type");

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        // Header views
        btnBack = findViewById(R.id.device_connect_btnBack);
        tvTitle = findViewById(R.id.device_connect_tvTitle);
        tvSubtitle = findViewById(R.id.device_connect_tvSubtitle);
        tvStepIndicator = findViewById(R.id.device_connect_tvStepIndicator);

        // AP Connect views
        tvNetworkName = findViewById(R.id.device_connect_tvNetworkName);
        tvNetworkPassword = findViewById(R.id.device_connect_tvNetworkPassword);
        btnPrevious = findViewById(R.id.device_connect_btnPrevious);
        btnNext = findViewById(R.id.device_connect_btnNext);
        btnOpenWifiSettings = findViewById(R.id.device_connect_btnOpenWifiSettings);

        // Set header text
        tvTitle.setText("Connect to Device");
        tvSubtitle.setText("Connect your phone to the device's WiFi network");
        tvStepIndicator.setText("Step 2 of 3");

        // FIXED: Use the same network name as ESP32 creates
        // ESP32 creates "SafeHome_7" based on DEVICE_ID in Config.h
        tvNetworkName.setText("SafeHome_7");
        tvNetworkPassword.setText("S@feHom3");
    }

    private void setupClickListeners() {
        // Back button
        btnBack.setOnClickListener(v -> onBackPressed());

        // Network credentials - tap to copy
        tvNetworkName.setOnClickListener(v -> {
            copyToClipboard("Network Name", tvNetworkName.getText().toString());
        });

        tvNetworkPassword.setOnClickListener(v -> {
            copyToClipboard("Network Password", tvNetworkPassword.getText().toString());
        });

        // WiFi Settings Button
        btnOpenWifiSettings.setOnClickListener(v -> openWifiSettings());

        // Previous button
        btnPrevious.setOnClickListener(v -> onBackPressed());

        // Next button
        btnNext.setOnClickListener(v -> {
            // Pass all data to device setup activity
            Intent intent = new Intent(this, DeviceSetupActivity.class);
            intent.putExtra("wifi_ssid", wifiSSID);
            intent.putExtra("wifi_password", wifiPassword);
            intent.putExtra("device_type", deviceType);
            intent.putExtra("device_network", tvNetworkName.getText().toString());
            intent.putExtra("device_password", tvNetworkPassword.getText().toString());
            startActivity(intent);
        });
    }

    private void copyToClipboard(String label, String text) {
        android.content.ClipboardManager clipboard =
                (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, label + " copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    private void openWifiSettings() {
        try {
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            String networkName = tvNetworkName.getText().toString();
            Toast.makeText(this, "Connect to " + networkName, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Could not open WiFi settings", Toast.LENGTH_SHORT).show();
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