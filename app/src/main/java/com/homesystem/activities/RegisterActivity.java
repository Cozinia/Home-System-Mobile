package com.homesystem.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.homesystem.R;
import com.homesystem.utils.Validator;

public class RegisterActivity extends AppCompatActivity {
    private EditText etFirstName, etLastName, etEmail, etPassword, etConfirmPassword;
    private Button btnCreateAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initComponents();
        btnCreateAccount.setOnClickListener(v -> getTextFromFields());
    }

    void initComponents() {
        etFirstName = findViewById(R.id.register_etFirstName);
        etLastName = findViewById(R.id.register_etLastName);
        etEmail = findViewById(R.id.register_etEmail);
        etPassword = findViewById(R.id.register_etPassword);
        etConfirmPassword = findViewById(R.id.register_etConfirmPassword);
        btnCreateAccount = findViewById(R.id.register_btnRegister);
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
        } else if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match!");
            isValid = false;
        }

        return isValid;
    }

    private void getTextFromFields() {
        if (!validateFields()) return;

        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        Log.d("REGISTER", "firstName: " + firstName + " lastName: " + lastName + " email: " + email + " password: " + password);
    }
}
