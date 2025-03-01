package com.example.grpasg;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class ReminderBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String description = intent.getStringExtra("description");

        // Create notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "reminder_channel";
        String channelName = "Reminders";
;
        if (description != null) {
            // Proceed to handle the reminder
        } else {
            Log.e("ReminderBroadcastReceiver", "Description is null");
        }

        // Create a notification channel (for Android O and later)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        // Notification click action
        Intent activityIntent = new Intent(context, ReminderActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Reminder")
                .setContentText(description)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        // Trigger the notification
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());

        // Automatically delete the triggered reminder
        Intent activitydelete = new Intent(context, ReminderActivity.class);
        activitydelete.putExtra("description_to_remove", description);
        activitydelete.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(activitydelete);
    }
}
