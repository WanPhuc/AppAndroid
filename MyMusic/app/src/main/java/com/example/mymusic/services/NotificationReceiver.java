package com.example.mymusic.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.core.content.ContextCompat;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;

        String action = intent.getAction();
        Log.d("NOTI_RECEIVER", "Received action: " + action); // ✅ Debug

        Intent serviceIntent = new Intent(context, MusicPlayerService.class);
        serviceIntent.setAction(action);
        serviceIntent.setPackage(context.getPackageName()); // ✅ Explicit Intent (Android 14+)

        try {
            ContextCompat.startForegroundService(context, serviceIntent);
            Log.d("NOTI_RECEIVER", "Foreground service started for: " + action);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("NOTI_RECEIVER", "Failed to start service: " + e.getMessage());
        }
    }
}
