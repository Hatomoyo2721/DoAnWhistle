package com.example.myapplication;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

//27 - 02 - 2024
//Create notification when play a song
public class ApplicationClass extends Application {
    public static final String CHANNEL_ID_1 = "CHANNEL_1";
    public static final String CHANNEL_ID_2 = "CHANNEL_2";
    public static final String ACTION_PREVIOUS = "PREVIOUS";
    public static final String ACTION_NEXT = "NEXT";
    public static final String ACTION_PLAY = "PLAY";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            NotificationChannel channel1 =
                    new NotificationChannel(CHANNEL_ID_1,
                            "Channel 1", NotificationManager.IMPORTANCE_HIGH);
            channel1.setDescription("Channel 1 Desc..");

            NotificationChannel channel2 =
                    new NotificationChannel(CHANNEL_ID_2,
                            "Channel 2", NotificationManager.IMPORTANCE_HIGH);
            channel2.setDescription("Channel 2 Desc..");

            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel1);
                notificationManager.createNotificationChannel(channel2);
            }
        }
    }
}
