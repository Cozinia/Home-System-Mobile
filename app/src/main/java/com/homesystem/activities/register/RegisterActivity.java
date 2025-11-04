package com.homesystem.activities.register;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.homesystem.R;
import com.homesystem.activities.dashboard.DashboardActivity;
import com.homesystem.models.User;
import com.homesystem.repositories.UserExistsCallback;
import com.homesystem.repositories.UserRegistrationCallback;
import com.homesystem.repositories.UserRepository;
import com.homesystem.utils.PasswordUtils;
import com.homesystem.utils.SessionManager;
import com.homesystem.utils.Validator;

public class RegisterActivity extends AppCompatActivity {
    private EditText etFirstName, etLastName, etEmail, etPassword, etConfirmPassword;
    private Button btnCreateAccount;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize session manager
        sessionManager = new SessionManager(this);

        initComponents();
        btnCreateAccount.setOnClickListener(v -> registerUser());
    }

    void initComponents() {
        etFirstName = findViewById(R.id.register_etFirstName);
        etLastName = findViewById(R.id.register_etLastName);
        etEmail = findViewById(R.id.register_etEmail);
        etPassword = findViewById(R.id.register_etPassword);
        etConfirmPassword = findViewById(R.id.register_etConfirmPassword);
        btnCreateAccount = findViewById(R.id.register_btnRegister);

        // Add back to login functionality
        TextView backToLogin = findViewById(R.id.register_tvBackToLogin);
        backToLogin.setOnClickListener(v -> {
            finish(); // Go back to login
        });
    }

    private boolean validateFields() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        boolean isValid = true;

        if (Validator.checkNullOrEmpty(firstName)) {
            etFirstName.setError("First name is required!");
            isValid = false;
        }
        if (Validator.checkNullOrEmpty(lastName)) {
            etLastName.setError("Last name is required!");
            isValid = false;
        }
        if (Validator.checkNullOrEmpty(email) || !Validator.checkRegexEmail(email)) {
            etEmail.setError("Valid email is required!");
            isValid = false;
        }
        if (Validator.checkNullOrEmpty(password)) {
            etPassword.setError("Password is required!");
            isValid = false;
        } else if (!Validator.checkRegexPassword(password)) {
            etPassword.setError("Password must contain at least 8 characters with uppercase, lowercase and numbers!");
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match!");
            isValid = false;
        }

        return isValid;
    }

    private void registerUser() {
        if (!validateFields()) return;

        // Disable button during registration
        btnCreateAccount.setEnabled(false);
        btnCreateAccount.setText("Creating Account...");

        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // First check if user already exists
        UserRepository.checkUserExists(email, new UserExistsCallback() {
            @Override
            public void onResult(boolean exists) {
                if (exists) {
                    btnCreateAccount.setEnabled(true);
                    btnCreateAccount.setText("Create Account");
                    etEmail.setError("User with this email already exists!");
                    Toast.makeText(RegisterActivity.this, "User already exists!", Toast.LENGTH_SHORT).show();
                } else {
                    // User doesn't exist, proceed with registration
                    createNewUser(firstName, lastName, email, password);
                }
            }

            @Override
            public void onError(String error) {
                btnCreateAccount.setEnabled(true);
                btnCreateAccount.setText("Create Account");
                Toast.makeText(RegisterActivity.this, "Error checking user: " + error, Toast.LENGTH_SHORT).show();
                Log.e("REGISTER", "Error checking user existence: " + error);
            }
        });
    }

    private void createNewUser(String firstName, String lastName, String email, String password) {
        // Hash the password
        String hashedPassword = PasswordUtils.hashPassword(password);

        // Create user object
        User newUser = new User(firstName, lastName, email, hashedPassword);

        // Generate and set token
        String token = PasswordUtils.generateToken();
        newUser.setToken(token);

        // Register user in database
        UserRepository.registerUser(newUser, new UserRegistrationCallback() {
            @Override
            public void onSuccess(String userId) {
                Log.d("REGISTER", "User registered successfully with ID: " + userId);

                // Set the user ID (important for session)
                newUser.setId(userId);

                // Create login session for the new user
                sessionManager.createLoginSession(newUser);

                // Show success message
                Toast.makeText(RegisterActivity.this, "Registration successful! Welcome!", Toast.LENGTH_SHORT).show();

                // Navigate to dashboard
                Intent dashboardIntent = new Intent(RegisterActivity.this, DashboardActivity.class);
                dashboardIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(dashboardIntent);
                finish();
            }

            @Override
            public void onError(String error) {
                btnCreateAccount.setEnabled(true);
                btnCreateAccount.setText("Create Account");
                Toast.makeText(RegisterActivity.this, "Registration failed: " + error, Toast.LENGTH_LONG).show();
                Log.e("REGISTER", "Registration error: " + error);
            }
        });
    }

    private void clearFields() {
        etFirstName.setText("");
        etLastName.setText("");
        etEmail.setText("");
        etPassword.setText("");
        etConfirmPassword.setText("");
    }

    @Override
    public void onBackPressed() {
        // Allow back navigation to login screen
        super.onBackPressed();
    }
}