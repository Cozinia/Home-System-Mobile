package com.homesystem.activities.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.homesystem.R;
import com.homesystem.activities.dashboard.central_units.CentralUnitsActivity;
import com.homesystem.activities.dashboard.sensors.SensorsActivity;
import com.homesystem.activities.login.MainActivity;
import com.homesystem.activities.profile.ProfileActivity;
import com.homesystem.activities.add_device.WiFiConfigActivity;
import com.homesystem.models.User;
import com.homesystem.utils.SessionManager;
import com.homesystem.utils.HomeSystemApplication;

public class DashboardActivity extends AppCompatActivity {
    private static final String TAG = "DashboardActivity";

    private SessionManager sessionManager;
    private TextView welcomeTextView;
    private User currentUser;

    // Bottom navigation elements
    private LinearLayout tabHome, tabAddDevice, tabProfile;
    private ImageView iconHome, iconAdd, iconProfile;
    private TextView textHome, textAdd, textProfile;

    private void loadSavedLanguage() {
        String savedLanguage = getSharedPreferences("AppSettings", MODE_PRIVATE)
                .getString("language", "en");
        HomeSystemApplication.setAppLocale(this, savedLanguage);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadSavedLanguage();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize session manager
        sessionManager = new SessionManager(this);

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            Log.w(TAG, "User not logged in, redirecting to login");
            redirectToLogin();
            return;
        }

        // Get current user
        currentUser = sessionManager.getCurrentUser();
        Log.d(TAG, "Dashboard loaded for user: " + (currentUser != null ? currentUser.getEmail() : "null"));

        // Initialize views
        initializeViews();

        // Display user info
        displayUserInfo();

        // Setup click listeners
        setupClickListeners();

        // Set home tab as selected by default
        selectTab(0);

        // Check if returning from device setup
        checkForNewDeviceAdded();
    }

    private void initializeViews() {
        welcomeTextView = findViewById(R.id.dashboard_tvWelcome);

        // Bottom navigation
        tabHome = findViewById(R.id.tab_home);
        tabAddDevice = findViewById(R.id.tab_add_device);
        tabProfile = findViewById(R.id.tab_profile);

        iconHome = findViewById(R.id.icon_home);
        iconAdd = findViewById(R.id.icon_add);
        iconProfile = findViewById(R.id.icon_profile);

        textHome = findViewById(R.id.text_home);
        textAdd = findViewById(R.id.text_add);
        textProfile = findViewById(R.id.text_profile);
    }

    private void displayUserInfo() {
        if (currentUser != null) {
            String welcomeMessage = currentUser.getFirstName() + " " + currentUser.getLastName();
            welcomeTextView.setText(welcomeMessage);
            Log.d(TAG, "Displaying info for user: " + currentUser.getEmail());
        } else {
            welcomeTextView.setText("Welcome Back!");
            Log.w(TAG, "Current user is null");
        }
    }

    private void setupClickListeners() {
        // Card click listeners
        CardView cardCentralUnits = findViewById(R.id.card_central_units);
        CardView cardSensors = findViewById(R.id.card_sensors);

        cardCentralUnits.setOnClickListener(v -> {
            Intent openCentralUnitsList = new Intent(this, CentralUnitsActivity.class);
            startActivity(openCentralUnitsList);
        });

        cardSensors.setOnClickListener(v -> {
            Intent openSensorsList = new Intent(this, SensorsActivity.class);
            startActivity(openSensorsList);
        });

        // Bottom navigation click listeners
        tabHome.setOnClickListener(v -> {
            selectTab(0);
            // Already on home
        });

        tabAddDevice.setOnClickListener(v -> {
            selectTab(1);
            // Start device setup flow
            Intent addDeviceIntent = new Intent(this, WiFiConfigActivity.class);
            startActivity(addDeviceIntent);
        });

        tabProfile.setOnClickListener(v -> {
            selectTab(2);
            Intent settingsIntent = new Intent(this, ProfileActivity.class);
            startActivity(settingsIntent);
        });
    }

    private void checkForNewDeviceAdded() {
        Intent intent = getIntent();
        if (intent != null) {
            boolean newDeviceAdded = intent.getBooleanExtra("new_device_added", false);
            String deviceNetwork = intent.getStringExtra("device_network");
            String deviceType = intent.getStringExtra("device_type");

            if (newDeviceAdded) {
                Log.d(TAG, "New device added: " + deviceNetwork + " (Type: " + deviceType + ")");

                // Show success message
                String message = "Central Unit added successfully!";
                if (deviceNetwork != null && !deviceNetwork.isEmpty()) {
                    message = deviceNetwork + " added successfully!";
                }
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();

                // You could also refresh device lists here if needed
                // refreshDeviceData();
            }
        }
    }

    private void selectTab(int selectedTab) {
        // Reset all tabs to unselected state
        resetTabColors();

        // Set selected tab colors
        switch (selectedTab) {
            case 0: // Home
                iconHome.setColorFilter(ContextCompat.getColor(this, R.color.blue_primary));
                textHome.setTextColor(ContextCompat.getColor(this, R.color.blue_primary));
                textHome.setTypeface(textHome.getTypeface(), android.graphics.Typeface.BOLD);
                break;
            case 1: // Add Device
                iconAdd.setColorFilter(ContextCompat.getColor(this, R.color.blue_primary));
                textAdd.setTextColor(ContextCompat.getColor(this, R.color.blue_primary));
                textAdd.setTypeface(textAdd.getTypeface(), android.graphics.Typeface.BOLD);
                break;
            case 2: // Profile
                iconProfile.setColorFilter(ContextCompat.getColor(this, R.color.blue_primary));
                textProfile.setTextColor(ContextCompat.getColor(this, R.color.blue_primary));
                textProfile.setTypeface(textProfile.getTypeface(), android.graphics.Typeface.BOLD);
                break;
        }
    }

    private void resetTabColors() {
        int unselectedColor = ContextCompat.getColor(this, R.color.gray_unselected);

        iconHome.setColorFilter(unselectedColor);
        iconAdd.setColorFilter(unselectedColor);
        iconProfile.setColorFilter(unselectedColor);

        textHome.setTextColor(unselectedColor);
        textAdd.setTextColor(unselectedColor);
        textProfile.setTextColor(unselectedColor);

        textHome.setTypeface(textHome.getTypeface(), android.graphics.Typeface.NORMAL);
        textAdd.setTypeface(textAdd.getTypeface(), android.graphics.Typeface.NORMAL);
        textProfile.setTypeface(textProfile.getTypeface(), android.graphics.Typeface.NORMAL);
    }

    private void logout() {
        Log.d(TAG, "User logging out");
        sessionManager.logout();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        checkForNewDeviceAdded();
    }

    @Override
    public void onBackPressed() {
        // Prevent going back to registration/login after successful login
        super.onBackPressed();
        moveTaskToBack(true);
    }
}