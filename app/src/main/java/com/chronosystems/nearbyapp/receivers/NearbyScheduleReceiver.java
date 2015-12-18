package com.chronosystems.nearbyapp.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import java.util.Calendar;

public class NearbyScheduleReceiver extends WakefulBroadcastReceiver {

    // restart service every 30 seconds
    private static final long REPEAT_TIME = 1000 * 30;

    public NearbyScheduleReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final AlarmManager service = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        final Intent i = new Intent(context, NearbyStartServiceReceiver.class);
        final PendingIntent pending = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);

        final Calendar cal = Calendar.getInstance();
        // start 30 seconds after boot completed
        cal.add(Calendar.SECOND, 30);

        // fetch every 30 seconds
        // InexactRepeating allows Android to optimize the energy consumption
        service.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), REPEAT_TIME, pending);

        //service.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), REPEAT_TIME, pending);
    }
}
