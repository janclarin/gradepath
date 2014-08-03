package com.janclarin.gradepath.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.janclarin.gradepath.activity.BaseActivity;
import com.janclarin.gradepath.model.Reminder;

public class ReminderService extends Service {

    /**
     * Class for clients to access.
     */
    public class ServiceBinder extends Binder {
        ReminderService getService() {
            return ReminderService.this;
        }
    }

    /* Receives interactions from clients. */
    private final IBinder mBinder = new ServiceBinder();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Run until stopped explicitly.
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Set an alarm for a reminder.
     */
    public void setAlarm(Reminder reminder) {
        new AlarmTask(this, reminder).run();
    }

    public class AlarmTask implements Runnable {

        private final Reminder reminder;
        private final AlarmManager alarmManager;
        private final Context context;

        public AlarmTask(Context context, Reminder reminder) {
            this.context = context;
            this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            this.reminder = reminder;
        }

        @Override
        public void run() {
            Intent intent = new Intent(context, NotifyService.class);
            intent.putExtra(BaseActivity.REMINDER_KEY, reminder);
            intent.putExtra(NotifyService.INTENT_NOTIFY, true);

            PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);

            alarmManager.set(AlarmManager.RTC, reminder.getReminderDate().getTimeInMillis(),
                    pendingIntent);
        }
    }
}
