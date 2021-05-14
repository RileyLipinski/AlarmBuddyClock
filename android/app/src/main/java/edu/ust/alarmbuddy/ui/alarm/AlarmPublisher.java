package edu.ust.alarmbuddy.ui.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import edu.ust.alarmbuddy.worker.alarm.AlarmFetchReceiver;
import edu.ust.alarmbuddy.worker.alarm.AlarmNoisemaker;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

public class AlarmPublisher {

	private static final AtomicInteger counter = new AtomicInteger(0);
	public static String CLASS = AlarmPublisher.class.getName();
	public static int TWO_MINUTES = 2 * 60 * 1000;
	public static int TWENTY_FOUR_HOURS = 24 * 60 * 60 * 1000;

	/**
	 * Receives a request to schedule an alarm and sets up a listener to either fetch an alarm from
	 * the API or play a default alarm.
	 *
	 * @param context The application context
	 * @param hours   The hours value for the scheduled alarm time
	 * @param minutes The minutes value for the scheduled alarm time
	 */
	public static void publishAlarm(Context context, int hours, int minutes) {
		long wakeupTime = wakeupTime(hours, minutes, System.currentTimeMillis());
		publishAlarm(context, wakeupTime);
	}

	/**
	 * Receives a request to schedule an alarm and sets up a listener to either fetch an alarm from
	 * the API or play a default alarm.
	 *
	 * @param context    The application context
	 * @param wakeupTime The system time in millis when the alarm should sound
	 */
	public static void publishAlarm(Context context, long wakeupTime) {
		Intent intent;
		PendingIntent pendingIntent;
		AlarmManager alarmManager = getAlarmManager(context);

		if (wakeupTime - (TWO_MINUTES) < System.currentTimeMillis()) {
			// set default alarm because there is not time to guarantee a successful fetch
			Log.i(CLASS, "Setting default alarm");
			intent = new Intent(context, AlarmNoisemaker.class);
			intent.putExtra("useDefaultNoise", true);
			pendingIntent = PendingIntent.getBroadcast(context, counter.getAndIncrement(), intent, 0);
			alarmManager.setExact(AlarmManager.RTC_WAKEUP, wakeupTime, pendingIntent);
		} else {
			Log.i(CLASS, "Scheduling alarm fetch");
			intent = new Intent(context, AlarmFetchReceiver.class);
			intent.putExtra("wakeupTime", wakeupTime);
			pendingIntent = PendingIntent.getBroadcast(context, counter.getAndIncrement(), intent, 0);
			alarmManager
				.setExact(AlarmManager.RTC_WAKEUP, wakeupTime - TWO_MINUTES, pendingIntent);
		}
	}

	/**
	 * Determines the time that the alarm should be set. If the alarm time today has already passed,
	 * it will be set for tomorrow.
	 *
	 * @param hours   The hours the alarm will be set for
	 * @param minutes The minute the alarm will be set for
	 * @param now     The current time, in milliseconds
	 *
	 * @return the time that the user alarm should play, relative to the system time
	 */
	public static long wakeupTime(int hours, int minutes, long now) {
		Calendar rightNow = Calendar.getInstance();
		rightNow.setTimeInMillis(now);

		Calendar todayAtTime = (Calendar) rightNow.clone();
		todayAtTime.set(Calendar.HOUR_OF_DAY, hours);
		todayAtTime.set(Calendar.MINUTE, minutes);
		todayAtTime.set(Calendar.SECOND, 0);
		todayAtTime.set(Calendar.MILLISECOND, 0);

		if (todayAtTime.after(rightNow)) {
			return todayAtTime.getTimeInMillis();
		} else {
			return todayAtTime.getTimeInMillis() + TWENTY_FOUR_HOURS;
		}
	}

	public static AlarmManager getAlarmManager(Context context) {
		return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	}
}
