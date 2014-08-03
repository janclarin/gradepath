package com.janclarin.gradepath.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.janclarin.gradepath.database.DatabaseFacade;
import com.janclarin.gradepath.model.Reminder;
import com.janclarin.gradepath.service.ReminderClient;

import java.util.List;

public class RemindersOnBoot extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        DatabaseFacade database = DatabaseFacade.getInstance(context.getApplicationContext());

        ReminderClient reminderClient = new ReminderClient(context);

        List<Reminder> upcomingReminders = database.getUpcomingReminders();

        for (Reminder reminder : upcomingReminders) {
            reminderClient.setAlarmForNotification(reminder);
        }
    }
}
