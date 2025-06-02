package com.homesystem.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.homesystem.R;
import com.homesystem.utils.Validator;

import java.util.Locale;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private Button btnResetPassword;
    private TextInputLayout etPasswordLayout, etConfirmPasswordLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        initComponents();
        btnResetPassword.setOnClickListener(v -> getTextFromFields());
    }

    private void initComponents() {
        etEmail = findViewById(R.id.login_et_email);
        etPassword = findViewById(R.id.forgotPassword_et_password);
        etConfirmPassword = findViewById(R.id.forgotPassword_et_confirmPassword);
        btnResetPassword = findViewById(R.id.forgotPassword_btnResetPassword);
        etPasswordLayout = findViewById(R.id.forgotPassword_et_password_layout);
        etConfirmPasswordLayout = findViewById(R.id.forgotPassword_et_confirm_password_layout);

        etPasswordLayout.setEndIconTintList(android.content.res.ColorStateList.valueOf(Color.BLACK));
        etConfirmPasswordLayout.setEndIconTintList(android.content.res.ColorStateList.valueOf(Color.BLACK));
    }

    private boolean validateFields() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        boolean isValid = true;

        if (Validator.checkNullOrEmpty(email) || !Validator.checkRegexEmail(email)) {
            etEmail.setError("Valid email is required!");
            isValid = false;
        }
        if (Validator.checkNullOrEmpty(password)) {
            etPassword.setError("Password is required!");
            etPasswordLayout.setEndIconMode(TextInputLayout.END_ICON_NONE);
            isValid = false;
        } else {
            etPasswordLayout.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match!");
            etConfirmPasswordLayout.setEndIconMode(TextInputLayout.END_ICON_NONE);
            isValid = false;
        } else {
            etConfirmPasswordLayout.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
        }

        return isValid;
    }

    private void getTextFromFields() {
        if (!validateFields()) return;

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        Log.d("FORGOT-PASSWORD", "email: " + email + " password: " + password);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences prefs = newBase.getSharedPreferences("Settings", MODE_PRIVATE);
        String language = prefs.getString("PreferedLang", "en"); // Default language is English

        Locale newLocale = new Locale(language);
        Locale.setDefault(newLocale);
        Configuration config = new Configuration();
        config.setLocale(newLocale);

        Context context = newBase.createConfigurationContext(config);
        super.attachBaseContext(context);
    }
}
