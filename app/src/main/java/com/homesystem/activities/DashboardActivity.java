package com.homesystem.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.homesystem.R;
import com.homesystem.models.User;
import com.homesystem.utils.SessionManager;

public class DashboardActivity extends AppCompatActivity {
    private static final String TAG = "DashboardActivity";

    private SessionManager sessionManager;
    private TextView welcomeTextView;
    private User currentUser;

    // Bottom navigation elements
    private LinearLayout tabHome, tabAddDevice, tabProfile;
    private ImageView iconHome, iconAdd, iconProfile;
    private TextView textHome, textAdd, textProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
            Toast.makeText(this, "Central Units - To be implemented", Toast.LENGTH_SHORT).show();
        });

        cardSensors.setOnClickListener(v -> {
            Toast.makeText(this, "Sensors - To be implemented", Toast.LENGTH_SHORT).show();
        });

        // Bottom navigation click listeners
        tabHome.setOnClickListener(v -> {
            selectTab(0);
            // Already on home
        });

        tabAddDevice.setOnClickListener(v -> {
            selectTab(1);
            Toast.makeText(this, "Add Device - To be implemented", Toast.LENGTH_SHORT).show();
        });

        tabProfile.setOnClickListener(v -> {
            selectTab(2);
            showProfileOptions();
        });
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

    private void showProfileOptions() {
        // Create a simple dialog or menu for profile options
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Profile Options")
                .setItems(new CharSequence[]{"View Profile", "Settings", "Logout"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            Toast.makeText(this, "View Profile - To be implemented", Toast.LENGTH_SHORT).show();
                            break;
                        case 1:
                            Toast.makeText(this, "Settings - To be implemented", Toast.LENGTH_SHORT).show();
                            break;
                        case 2:
                            logout();
                            break;
                    }
                })
                .show();
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
    public void onBackPressed() {
        // Prevent going back to registration/login after successful login
        moveTaskToBack(true);
    }
}