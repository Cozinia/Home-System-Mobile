package com.homesystem.activities.forgot_password;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.homesystem.R;
import com.homesystem.utils.Validator;

public class ForgotPasswordActivity extends AppCompatActivity {
    private static final String TAG = "ForgotPasswordActivity";

    // UI Elements
    private EditText etEmail, etPassword, etConfirmPassword;
    private Button btnResetPassword;
    private TextView tvBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize UI elements
        initComponents();

        // Set up click listeners
        setupClickListeners();
    }

    private void initComponents() {
        // Initialize input fields
        etEmail = findViewById(R.id.forgotPassword_et_email);
        etPassword = findViewById(R.id.forgotPassword_et_password);
        etConfirmPassword = findViewById(R.id.forgotPassword_et_confirmPassword);

        // Initialize buttons
        btnResetPassword = findViewById(R.id.forgotPassword_btnResetPassword);
        tvBackToLogin = findViewById(R.id.forgotPassword_tvBackToLogin);
    }

    private void setupClickListeners() {
        // Reset password button click
        btnResetPassword.setOnClickListener(v -> resetPassword());

        // Back to login click
        tvBackToLogin.setOnClickListener(v -> {
            // Go back to login activity
            finish();
        });
    }

    private boolean validateFields() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        boolean isValid = true;

        // Validate email
        if (Validator.checkNullOrEmpty(email) || !Validator.checkRegexEmail(email)) {
            etEmail.setError("Valid email is required!");
            isValid = false;
        }

        // Validate password
        if (Validator.checkNullOrEmpty(password)) {
            etPassword.setError("New password is required!");
            isValid = false;
        } else if (!Validator.checkRegexPassword(password)) {
            etPassword.setError("Password must contain at least 8 characters with uppercase, lowercase and numbers!");
            isValid = false;
        }

        // Validate confirm password
        if (Validator.checkNullOrEmpty(confirmPassword)) {
            etConfirmPassword.setError("Please confirm your password!");
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match!");
            isValid = false;
        }

        return isValid;
    }

    private void resetPassword() {
        if (!validateFields()) return;

        // Disable button during reset process
        btnResetPassword.setEnabled(false);
        btnResetPassword.setText("Resetting...");

        String email = etEmail.getText().toString().trim();
        String newPassword = etPassword.getText().toString().trim();

        // TODO: Implement actual password reset logic here
        // For now, just show a success message
        Log.d(TAG, "Password reset requested for email: " + email);

        // Simulate processing time
        btnResetPassword.postDelayed(() -> {
            // Re-enable button
            btnResetPassword.setEnabled(true);
            btnResetPassword.setText(getString(R.string.reset_password_btn_resetPassword));

            // Show success message
            Toast.makeText(this, "Password reset successful! Please login with your new password.", Toast.LENGTH_LONG).show();

            // Go back to login
            finish();
        }, 2000);
    }

    @Override
    public void onBackPressed() {
        // Allow back navigation to login screen
        super.onBackPressed();
    }
}