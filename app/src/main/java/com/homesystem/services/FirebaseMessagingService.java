package com.homesystem.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;
import com.homesystem.R;
import com.homesystem.activities.MainActivity;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "home_system_notifications";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "Message received from: " + remoteMessage.getFrom());

        // Handle data payload
        if (remoteMessage.getData().size() > 0) {
            handleDataMessage(remoteMessage);
        }

        // Handle notification payload
        if (remoteMessage.getNotification() != null) {
            showNotification(
                    remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getBody(),
                    remoteMessage.getData()
            );
        }
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "New FCM token: " + token);

        // Send token to your server
        sendRegistrationToServer(token);
    }

    private void handleDataMessage(RemoteMessage remoteMessage) {
        String alertType = remoteMessage.getData().get("alert_type");
        String deviceId = remoteMessage.getData().get("device_id");

        // Process different alert types
        if ("motion_detected".equals(alertType)) {
            showNotification(
                    "Motion Detected!",
                    "Motion detected by sensor: " + deviceId,
                    remoteMessage.getData()
            );
        } else if ("door_opened".equals(alertType)) {
            showNotification(
                    "Door Alert!",
                    "Door opened - Device: " + deviceId,
                    remoteMessage.getData()
            );
        }
    }

    private void showNotification(String title, String body, java.util.Map<String, String> data) {
        Intent intent = new Intent(this, MainActivity.class);

        // Add data to intent if needed
        if (data != null) {
            for (java.util.Map.Entry<String, String> entry : data.entrySet()) {
                intent.putExtra(entry.getKey(), entry.getValue());
            }
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.home_system_icon)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Home System Notifications";
            String description = "Notifications for home security alerts";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.enableVibration(true);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendRegistrationToServer(String token) {
        // TODO: Implement server communication to store the token
        Log.d(TAG, "Token ready to send to server");
    }
}