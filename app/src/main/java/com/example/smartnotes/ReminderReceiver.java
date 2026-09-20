package com.example.smartnotes;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "smart_notes_reminders";

    @Override
    public void onReceive(Context context, Intent intent) {

        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");

        if (title == null) {
            title = "Smart Notes Reminder";
        }

        if (content == null) {
            content = "You have a note reminder.";
        }

        NotificationManager notificationManager =
                (NotificationManager)
                        context.getSystemService(
                                Context.NOTIFICATION_SERVICE
                        );

        // Create notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Note Reminders",
                            NotificationManager.IMPORTANCE_HIGH
                    );

            channel.setDescription(
                    "Reminders from Smart Notes"
            );

            notificationManager.createNotificationChannel(
                    channel
            );
        }

        // Open app when notification is tapped
        Intent openIntent =
                new Intent(
                        context,
                        MainActivity.class
                );

        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        openIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT |
                                PendingIntent.FLAG_IMMUTABLE
                );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(
                        context,
                        CHANNEL_ID
                )
                        .setSmallIcon(
                                android.R.drawable.ic_dialog_info
                        )
                        .setContentTitle(
                                "⏰ " + title
                        )
                        .setContentText(
                                content
                        )
                        .setStyle(
                                new NotificationCompat.BigTextStyle()
                                        .bigText(content)
                        )
                        .setPriority(
                                NotificationCompat.PRIORITY_HIGH
                        )
                        .setAutoCancel(true)
                        .setContentIntent(
                                pendingIntent
                        );

        notificationManager.notify(
                (int) System.currentTimeMillis(),
                builder.build()
        );
    }
}