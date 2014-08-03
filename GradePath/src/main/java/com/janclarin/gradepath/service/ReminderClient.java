package com.janclarin.gradepath.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.janclarin.gradepath.model.Reminder;

public class ReminderClient {

    private ReminderService mBoundService;
    private Context mContext;
    private boolean mIsBound;

    public ReminderClient(Context context) {
        mContext = context;
    }

    /**
     * If successfully connected service, instantiate service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBoundService = ((ReminderService.ServiceBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBoundService = null;
        }
    };

    /**
     * Connects activity to service.
     */
    public void doBindService() {
        // Establish connection with our service.
        mContext.bindService(new Intent(mContext, ReminderService.class),
                mConnection, Context.BIND_AUTO_CREATE);
    }

    public void setAlarmForNotification(Reminder reminder) {
        mBoundService.setAlarm(reminder);
    }

    public void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            mContext.unbindService(mConnection);
            mIsBound = false;
        }
    }
}
