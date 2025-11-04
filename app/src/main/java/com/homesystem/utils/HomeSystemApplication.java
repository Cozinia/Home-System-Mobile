package com.homesystem.utils;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

public class HomeSystemApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Apply saved language when app starts
        applySavedLanguage();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Reapply saved language when configuration changes
        applySavedLanguage();
    }

    private void applySavedLanguage() {
        String savedLanguage = getSharedPreferences("AppSettings", MODE_PRIVATE)
                .getString("language", "en");
        setAppLocale(this, savedLanguage);
    }

    public static void setAppLocale(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);

        // Update configuration
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        // Also update application context
        Context applicationContext = context.getApplicationContext();
        Resources appResources = applicationContext.getResources();
        Configuration appConfiguration = appResources.getConfiguration();
        appConfiguration.setLocale(locale);
        appResources.updateConfiguration(appConfiguration, appResources.getDisplayMetrics());
    }
}