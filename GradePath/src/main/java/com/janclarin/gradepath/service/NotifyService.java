package com.janclarin.gradepath.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.os.IBinder;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.activity.BaseActivity;
import com.janclarin.gradepath.activity.MainActivity;
import com.janclarin.gradepath.model.Reminder;

public class NotifyService extends Service {

    // Unique ID to identify the notification.
    public static final String INTENT_NOTIFY = "com.janclarin.gradepath.service";
    private final IBinder mBinder = new ServiceBinder();
    private NotificationManager mNotificationManager;

    /**
     * Class for clients to access.
     */
    public class ServiceBinder extends Binder {
        NotifyService getService() {
            return NotifyService.this;
        }
    }

    @Override
    public void onCreate() {
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Reminder reminder = (Reminder) intent.getSerializableExtra(BaseActivity.REMINDER_KEY);

        if (intent.getBooleanExtra(INTENT_NOTIFY, false))
            showNotification(reminder);

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void showNotification(Reminder reminder) {
        Intent resultIntent = new Intent(this, MainActivity.class);

        // Creates artificial back stack for started activity. Clicking back leads out of the app.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent pendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        Notification notification =
                new Notification.Builder(this)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle(reminder.getName())
                        .setContentText(getString(R.string.reminder))
                        .setVibrate(new long[]{0, 100, 70})
                        .setContentIntent(pendingIntent)
                        .setLights(Color.alpha(R.color.notification_color), 500, 100).build();

        notification.defaults = Notification.DEFAULT_SOUND;
        notification.flags = Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_ONLY_ALERT_ONCE
                | Notification.FLAG_AUTO_CANCEL;

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(
                (int) reminder.getId(),
                notification
        );

        // Stop service when finished.
        stopSelf();
    }
}
