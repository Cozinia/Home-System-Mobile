package com.homesystem.activities.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.homesystem.utils.HomeSystemApplication;
import com.homesystem.R;
import com.homesystem.activities.login.MainActivity;
import com.homesystem.models.User;
import com.homesystem.utils.SessionManager;

import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private User currentUser;

    private TextView tvFullName, tvEmail, tvCurrentLanguage;
    private CardView cardEditProfile, cardLanguage, cardLogout, cardDeleteAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadSavedLanguage();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        currentUser = sessionManager.getCurrentUser();

        initializeViews();
        displayUserInfo();
        setupClickListeners();
    }

    private void loadSavedLanguage() {
        String savedLanguage = getSharedPreferences("AppSettings", MODE_PRIVATE)
                .getString("language", "en");
        HomeSystemApplication.setAppLocale(this, savedLanguage);
    }

    private void initializeViews() {
        tvFullName = findViewById(R.id.profile_tvFullName);
        tvEmail = findViewById(R.id.profile_tvEmail);
        tvCurrentLanguage = findViewById(R.id.profile_tvCurrentLanguage);
        cardEditProfile = findViewById(R.id.profile_cardEditProfile);
        cardLanguage = findViewById(R.id.profile_cardLanguage);
        cardLogout = findViewById(R.id.profile_cardLogout);
        cardDeleteAccount = findViewById(R.id.profile_cardDeleteAccount);
    }

    private void displayUserInfo() {
        if (currentUser != null) {
            String fullName = currentUser.getFirstName() + " " + currentUser.getLastName();
            tvFullName.setText(fullName);
            tvEmail.setText(currentUser.getEmail());
        } else {
            tvFullName.setText("User Name");
            tvEmail.setText("user@example.com");
        }

        updateCurrentLanguageDisplay();
    }

    private void updateCurrentLanguageDisplay() {
        String currentLanguage = getSharedPreferences("AppSettings", MODE_PRIVATE)
                .getString("language", "en");

        if ("ro".equals(currentLanguage)) {
            tvCurrentLanguage.setText(getString(R.string.română));
        } else {
            tvCurrentLanguage.setText(getString(R.string.english));
        }
    }

    private void setupClickListeners() {
        findViewById(R.id.profile_btnBack).setOnClickListener(v -> finish());

        cardEditProfile.setOnClickListener(v -> {
            Intent editProfileIntent = new Intent(this, EditProfileActivity.class);
            startActivity(editProfileIntent);
        });

        cardLanguage.setOnClickListener(v -> showLanguageDialog());

        cardLogout.setOnClickListener(v -> showLogoutDialog());

        cardDeleteAccount.setOnClickListener(v ->
                Toast.makeText(this, getString(R.string.delete_account) + " - Coming soon!", Toast.LENGTH_SHORT).show());
    }

    private void showLanguageDialog() {
        String[] languages = {getString(R.string.english), getString(R.string.română)};
        String[] languageCodes = {"en", "ro"};

        String currentLanguage = getSharedPreferences("AppSettings", MODE_PRIVATE)
                .getString("language", "en");

        int selectedIndex = "ro".equals(currentLanguage) ? 1 : 0;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Language / Selectează Limba")
                .setSingleChoiceItems(languages, selectedIndex, (dialog, which) -> {
                    String selectedLanguageCode = languageCodes[which];

                    if (!selectedLanguageCode.equals(currentLanguage)) {
                        changeLanguage(selectedLanguageCode);
                    }

                    dialog.dismiss();
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }


    private void changeLanguage(String languageCode) {
        // Save the language preference to SharedPreferences
        getSharedPreferences("AppSettings", MODE_PRIVATE)
                .edit()
                .putString("language", languageCode)
                .apply();

        HomeSystemApplication.setAppLocale(this, languageCode);

        // Restart doar ProfileActivity, păstrând stack-ul
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
        finish();

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.logout))
                .setMessage(getString(R.string.sign_out_of_your_account) + "?")
                .setPositiveButton(getString(R.string.logout), (dialog, which) -> logout())
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void logout() {
        sessionManager.logout();
        Toast.makeText(this, getString(R.string.logged_out_successfully), Toast.LENGTH_SHORT).show();
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentUser = sessionManager.getCurrentUser();
        displayUserInfo();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}