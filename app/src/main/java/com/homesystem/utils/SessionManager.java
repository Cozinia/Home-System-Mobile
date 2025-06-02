package com.homesystem.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.homesystem.models.User;

public class SessionManager {
    private static final String TAG = "SessionManager";
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_FIRST_NAME = "userFirstName";
    private static final String KEY_USER_LAST_NAME = "userLastName";
    private static final String KEY_USER_TOKEN = "userToken";

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public void createLoginSession(User user) {
        Log.d(TAG, "Creating login session for user: " + user.getEmail());

        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, user.getId());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.putString(KEY_USER_FIRST_NAME, user.getFirstName());
        editor.putString(KEY_USER_LAST_NAME, user.getLastName());
        editor.putString(KEY_USER_TOKEN, user.getToken());
        editor.apply();

        Log.d(TAG, "Login session created successfully");
    }

    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public User getCurrentUser() {
        if (!isLoggedIn()) {
            return null;
        }

        User user = new User();
        user.setId(preferences.getString(KEY_USER_ID, null));
        user.setEmail(preferences.getString(KEY_USER_EMAIL, null));
        user.setFirstName(preferences.getString(KEY_USER_FIRST_NAME, null));
        user.setLastName(preferences.getString(KEY_USER_LAST_NAME, null));
        user.setToken(preferences.getString(KEY_USER_TOKEN, null));

        return user;
    }

    public String getCurrentUserId() {
        return preferences.getString(KEY_USER_ID, null);
    }

    public String getCurrentUserEmail() {
        return preferences.getString(KEY_USER_EMAIL, null);
    }

    public void logout() {
        Log.d(TAG, "Logging out user");

        editor.clear();
        editor.apply();

        Log.d(TAG, "User logged out successfully");
    }

    public void updateUserToken(String token) {
        editor.putString(KEY_USER_TOKEN, token);
        editor.apply();
    }
}