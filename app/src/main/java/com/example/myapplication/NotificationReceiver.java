package com.example.myapplication;

import static com.example.myapplication.ApplicationClass.ACTION_NEXT;
import static com.example.myapplication.ApplicationClass.ACTION_PLAY;
import static com.example.myapplication.ApplicationClass.ACTION_PREVIOUS;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String actionName = intent.getAction();
        Intent serviceIntent = new Intent(context, MusicService.class);
        if (actionName != null) {
            switch (actionName) {
                case ACTION_PLAY:
//                    serviceIntent.putExtra("ActionName", intent.getAction());
                    serviceIntent.putExtra("ActionName", "playPause");
                    context.startService(serviceIntent);
                    break;

                case ACTION_NEXT:
//                    serviceIntent.putExtra("ActionName", intent.getAction());
                    serviceIntent.putExtra("ActionName", "Next");

                    context.startService(serviceIntent);
                    break;

                case ACTION_PREVIOUS:
//                    serviceIntent.putExtra("ActionName", intent.getAction());
                    serviceIntent.putExtra("ActionName", "Previous");
                    context.startService(serviceIntent);
                    break;
            }
        }
    }
}
