package com.scc.main1;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String ACTION_REFRESH_DATA = "com.scc.main1.getevent.REFRESH_DATA";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AlarmReceiver", "Alarm received");

        showNotification(context, "Periodic Task", "Data fetched and processed");
        Intent broadcastIntent = new Intent(ACTION_REFRESH_DATA);
        context.sendBroadcast(broadcastIntent);
    }

    @SuppressLint("MissingPermission")
    private void showNotification(Context context, String title, String content) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channel Name";
            String description = "Channel Description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("alarm_test", name, importance);
            channel.setDescription(description);

            // Register the channel with the system
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        Intent gsloginIntent = new Intent(context, Gslogin.class);
        gsloginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, gsloginIntent, PendingIntent.FLAG_MUTABLE);

        // Build and show the notification with the PendingIntent
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "alarm_test")
                .setSmallIcon(R.drawable.ccelogologo)
                .setContentTitle("CCE 새로운 일정 알림")
                .setContentText("관심있어 하시는 새로운 일정이 추가되었어요!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)  // Set the PendingIntent
                .setAutoCancel(true);  // Automatically remove the notification when tapped

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, builder.build());
    }
}
