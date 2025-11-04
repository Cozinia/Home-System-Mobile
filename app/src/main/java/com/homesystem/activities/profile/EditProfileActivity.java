package com.homesystem.activities.profile;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.homesystem.R;
import com.homesystem.models.User;
import com.homesystem.utils.PasswordUtils;
import com.homesystem.utils.SessionManager;
import com.homesystem.utils.Validator;

public class EditProfileActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private User currentUser;

    private ImageView btnBack;
    private TextInputEditText etFirstName, etLastName, etEmail;
    private TextInputEditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            finish();
            return;
        }

        currentUser = sessionManager.getCurrentUser();

        initializeViews();
        loadUserData();
        setupClickListeners();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.editProfile_btnBack);
        etFirstName = findViewById(R.id.editProfile_etFirstName);
        etLastName = findViewById(R.id.editProfile_etLastName);
        etEmail = findViewById(R.id.editProfile_etEmail);
        etCurrentPassword = findViewById(R.id.editProfile_etCurrentPassword);
        etNewPassword = findViewById(R.id.editProfile_etNewPassword);
        etConfirmPassword = findViewById(R.id.editProfile_etConfirmPassword);
        btnSave = findViewById(R.id.editProfile_btnSave);
    }

    private void loadUserData() {
        if (currentUser != null) {
            etFirstName.setText(currentUser.getFirstName());
            etLastName.setText(currentUser.getLastName());
            etEmail.setText(currentUser.getEmail());
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveChanges());
    }

    private boolean validatePersonalInfo() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        boolean isValid = true;

        if (Validator.checkNullOrEmpty(firstName)) {
            etFirstName.setError(getString(R.string.first_name_required));
            isValid = false;
        }

        if (Validator.checkNullOrEmpty(lastName)) {
            etLastName.setError(getString(R.string.last_name_required));
            isValid = false;
        }

        return isValid;
    }

    private boolean validatePasswordChange() {
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (Validator.checkNullOrEmpty(currentPassword) &&
                Validator.checkNullOrEmpty(newPassword) &&
                Validator.checkNullOrEmpty(confirmPassword)) {
            return true;
        }

        boolean isValid = true;

        if (Validator.checkNullOrEmpty(currentPassword)) {
            etCurrentPassword.setError(getString(R.string.current_password_required));
            isValid = false;
        }

        if (Validator.checkNullOrEmpty(newPassword)) {
            etNewPassword.setError(getString(R.string.new_password_required));
            isValid = false;
        } else if (!Validator.checkRegexPassword(newPassword)) {
            etNewPassword.setError(getString(R.string.password_requirements));
            isValid = false;
        }

        if (Validator.checkNullOrEmpty(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.confirm_password_required));
            isValid = false;
        } else if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.passwords_do_not_match));
            isValid = false;
        }

        if (!Validator.checkNullOrEmpty(currentPassword) && currentUser != null) {
            String hashedCurrentPassword = PasswordUtils.hashPassword(currentPassword);
            if (!hashedCurrentPassword.equals(currentUser.getHashedPassword())) {
                etCurrentPassword.setError(getString(R.string.current_password_incorrect));
                isValid = false;
            }
        }

        return isValid;
    }

    private void saveChanges() {
        if (!validatePersonalInfo() || !validatePasswordChange()) {
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText(getString(R.string.saving));

        try {
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();

            currentUser.setFirstName(firstName);
            currentUser.setLastName(lastName);

            String newPassword = etNewPassword.getText().toString().trim();
            if (!Validator.checkNullOrEmpty(newPassword)) {
                String hashedNewPassword = PasswordUtils.hashPassword(newPassword);
                currentUser.setHashedPassword(hashedNewPassword);
            }

            currentUser.setLastUpdatedAt(System.currentTimeMillis());
            sessionManager.createLoginSession(currentUser);

            btnSave.setEnabled(true);
            btnSave.setText(getString(R.string.save_changes));

            Toast.makeText(this, getString(R.string.profile_updated_successfully), Toast.LENGTH_SHORT).show();

            etCurrentPassword.setText("");
            etNewPassword.setText("");
            etConfirmPassword.setText("");

        } catch (Exception e) {
            btnSave.setEnabled(true);
            btnSave.setText(getString(R.string.save_changes));
            Toast.makeText(this, getString(R.string.error_updating_profile), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}