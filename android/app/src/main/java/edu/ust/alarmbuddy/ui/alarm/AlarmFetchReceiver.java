package edu.ust.alarmbuddy.ui.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmFetchReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO implement fetch of custom alarm sounds from database
		Log.i(getClass().toString(),"Fetching alarm noise from database");

		long wakeupTime = intent.getLongExtra("wakeupTime", Long.MIN_VALUE);

		if (wakeupTime < System.currentTimeMillis()) {
			throw new RuntimeException(
				"Tried to schedule an alarm for earlier than the current time");
		}

		PendingIntent pendingIntent = PendingIntent
			.getBroadcast(context, 0, new Intent(context, AlarmNoisemaker.class), 0);
		AlarmManager alarmManager = AlarmPublisher.getAlarmManager(context);

		alarmManager.setExact(AlarmManager.RTC_WAKEUP, wakeupTime, pendingIntent);
	}
}
