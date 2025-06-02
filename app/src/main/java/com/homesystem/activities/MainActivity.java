package com.homesystem.activities;

import android.content.res.Configuration;
import android.content.res.Resources;
import java.util.Locale;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.homesystem.R;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ActivityResultLauncher<String> requestPermissionLauncher;

    // UI Elements
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button registerTextView;
    private Button forgotPasswordTextView;
    private ImageButton englishLanguageButton;
    private ImageButton romanianLanguageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_HomeSystem);
        super.onCreate(savedInstanceState);
        com.homesystem.utils.SessionManager sessionManager = new com.homesystem.utils.SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            // User is already logged in, redirect to dashboard
            Intent intent = new Intent(this, com.homesystem.activities.DashboardActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Load saved language
        loadSavedLanguage();

        // Initialize UI elements
        initializeViews();

        // Initialize permission launcher
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> initializeFCM()
        );

        // Check and request notification permission
        checkNotificationPermission();

        // Set up click listeners
        setupClickListeners();
    }

    private void initializeViews() {
        // Match the IDs from your existing layout - EXACT SAME AS BEFORE
        emailEditText = findViewById(R.id.login_et_email);
        passwordEditText = findViewById(R.id.login_etPassword);
        loginButton = findViewById(R.id.login_btnLogin);
        registerTextView = findViewById(R.id.login_btnCreateAccount);
        forgotPasswordTextView = findViewById(R.id.login_btnForgotPassword);
        englishLanguageButton = findViewById(R.id.login_ibEnLang);
        romanianLanguageButton = findViewById(R.id.login_ibRoLang);
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });

        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to register activity
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to forgot password activity
                Intent intent = new Intent(MainActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        // Language change buttons
        englishLanguageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeLanguage("en");
            }
        });

        romanianLanguageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeLanguage("ro");
            }
        });
    }

    private void performLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Basic validation
        if (email.isEmpty()) {
            emailEditText.setError("Email is required");
            return;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Password is required");
            return;
        }

        // Validate email format
        if (!com.homesystem.utils.Validator.checkRegexEmail(email)) {
            emailEditText.setError("Please enter a valid email");
            return;
        }

        // Disable login button during login process
        loginButton.setEnabled(false);
        loginButton.setText("Signing in...");

        // Attempt login
        com.homesystem.repositories.UserRepository.loginUser(email, password, new com.homesystem.repositories.UserLoginCallback() {
            @Override
            public void onSuccess(com.homesystem.models.User user) {
                Log.d(TAG, "Login successful for user: " + user.getEmail());

                // Create session
                com.homesystem.utils.SessionManager sessionManager = new com.homesystem.utils.SessionManager(MainActivity.this);
                sessionManager.createLoginSession(user);

                // Show success message
                Toast.makeText(MainActivity.this, "Welcome back!", Toast.LENGTH_SHORT).show();

                // Navigate to dashboard
                Intent intent = new Intent(MainActivity.this, com.homesystem.activities.DashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Login failed: " + error);

                // Re-enable login button
                loginButton.setEnabled(true);
                loginButton.setText("Sign In");

                // Show error message
                Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();

                // Clear password field for security
                passwordEditText.setText("");
            }
        });
    }
    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                initializeFCM();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            initializeFCM();
        }
    }

    private void initializeFCM() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        String token = task.getResult();
                        Log.d(TAG, "FCM Token: " + token);

                        // Send token to your server
                        sendTokenToServer(token);
                    }
                });

        // Subscribe to topics if needed
        FirebaseMessaging.getInstance().subscribeToTopic("home_alerts")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Subscribed to home_alerts topic");
                        }
                    }
                });
    }

    private void sendTokenToServer(String token) {
        Log.d(TAG, "Token ready to send to server");
    }

    private void changeLanguage(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);

        // Update configuration
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        // Save the language preference
        getSharedPreferences("AppSettings", MODE_PRIVATE)
                .edit()
                .putString("language", languageCode)
                .apply();

        // Restart the activity to apply language changes
        Intent intent = getIntent();
        finish();
        startActivity(intent);

    }

    private void loadSavedLanguage() {
        String savedLanguage = getSharedPreferences("AppSettings", MODE_PRIVATE)
                .getString("language", "en"); // Default to English

        if (!savedLanguage.equals("en")) {
            changeLanguageWithoutRestart(savedLanguage);
        }
    }

    private void changeLanguageWithoutRestart(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }


}