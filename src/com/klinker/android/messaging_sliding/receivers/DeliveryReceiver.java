package com.klinker.android.messaging_sliding.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.Toast;
import com.klinker.android.messaging_donate.R;

public class DeliveryReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean result = intent.getBooleanExtra("result", true);
        int reportType = Integer.parseInt(sharedPrefs.getString("delivery_reports_type", "2"));

        switch (reportType) {
            case 1:
                // do nothing, just save and show a checkmark.
                break;
            case 2:
                // give a toast
                Toast.makeText(context, result ? context.getString(R.string.message_delivered) : context.getString(R.string.message_not_delivered), Toast.LENGTH_SHORT).show();
                break;
            case 3:
                // ugh. give a notification
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

                if (result) {
                    TextMessageReceiver.setIcon(builder, context);
                    builder.setPriority(Notification.PRIORITY_LOW);
                    builder.setContentTitle(context.getString(R.string.message_delivered));
                    builder.setTicker(context.getString(R.string.message_delivered));
                } else {
                    builder.setSmallIcon(R.drawable.ic_alert);
                    builder.setPriority(Notification.PRIORITY_HIGH);
                    builder.setContentTitle(context.getString(R.string.message_not_delivered));
                    builder.setTicker(context.getString(R.string.message_not_delivered));
                }

                builder.setAutoCancel(true);

                Intent resultIntent = new Intent(context, com.klinker.android.messaging_donate.MainActivity.class);

                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addParentStack(com.klinker.android.messaging_donate.MainActivity.class);
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_CANCEL_CURRENT
                        );

                builder.setContentIntent(resultPendingIntent);

                long[] pattern = {0L, 400L, 100L, 400L};
                builder.setVibrate(pattern);
                builder.setLights(0xFFffffff, 1000, 2000);

                NotificationManager mNotificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                Notification notification = builder.build();
                mNotificationManager.notify(4, notification);
                break;
        }
    }
}
