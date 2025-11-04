package com.homesystem.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.homesystem.models.CentralUnit;
import com.homesystem.models.Sensor;
import com.homesystem.models.User;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SessionManager {
    private static final String TAG = "SessionManager";
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_FIRST_NAME = "userFirstName";
    private static final String KEY_USER_LAST_NAME = "userLastName";
    private static final String KEY_USER_TOKEN = "userToken";
    private static final String KEY_CENTRAL_UNITS = "centralUnits";
    private static final String KEY_SENSORS = "sensors";

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Context context;
    private Gson gson;

    public SessionManager(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
        gson = new Gson();
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

    public String getAuthToken() {
        return preferences.getString(KEY_USER_TOKEN, null);
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

    public void saveCentralUnits(List<CentralUnit> centralUnits) {
        String json = gson.toJson(centralUnits);
        editor.putString(KEY_CENTRAL_UNITS, json);
        editor.apply();
        Log.d(TAG, "Central units saved: " + json);
    }

    public List<CentralUnit> getCentralUnits() {
        String json = preferences.getString(KEY_CENTRAL_UNITS, null);
        if (json != null) {
            Type type = new TypeToken<List<CentralUnit>>(){}.getType();
            List<CentralUnit> units = gson.fromJson(json, type);
            return units != null ? units : new ArrayList<>();
        }
        return new ArrayList<>();
    }

    public void saveSensors(List<Sensor> sensors) {
        String json = gson.toJson(sensors);
        editor.putString(KEY_SENSORS, json);
        editor.apply();
        Log.d(TAG, "Sensors saved: " + json);
    }

    public List<Sensor> getSensors() {
        String json = preferences.getString(KEY_SENSORS, null);
        if (json != null) {
            Type type = new TypeToken<List<Sensor>>(){}.getType();
            List<Sensor> sensors = gson.fromJson(json, type);
            return sensors != null ? sensors : new ArrayList<>();
        }
        return new ArrayList<>();
    }
}