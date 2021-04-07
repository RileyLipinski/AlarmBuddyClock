package edu.ust.alarmbuddy.ui.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import java.util.Calendar;

public class AlarmPublisher {

	public static int FIVE_MINUTES = 5 * 60 * 1000;
	public static int TWENTY_FOUR_HOURS = 24 * 60 * 60 * 1000;

	public static void publishAlarm(Context context, int hours, int minutes) {
		long wakeupTime = wakeupTime(hours, minutes, System.currentTimeMillis());

		Intent intent;
		PendingIntent pendingIntent;
		AlarmManager alarmManager = getAlarmManager(context);

//		if(wakeupTime - (FIVE_MINUTES * 2L) < System.currentTimeMillis()) {
//		    Log.i(AlarmPublisher.class.getName(),"Setting default alarm");
//		    intent = new Intent(context,AlarmNoisemaker.class);
//		    pendingIntent = PendingIntent.getBroadcast(context,0,intent,0);
//		    alarmManager.setExact(AlarmManager.RTC_WAKEUP,wakeupTime,pendingIntent);
//        } else {
//			Log.i(AlarmPublisher.class.getName(),"Scheduling fetch");
//			intent = new Intent(context, AlarmFetchReceiver.class);
//			intent.putExtra("wakeupTime", wakeupTime);
//			pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
//			alarmManager.setExact(AlarmManager.RTC_WAKEUP, wakeupTime - FIVE_MINUTES, pendingIntent);
//			AlarmNoisemaker.makeDefaultNoise(context);
//        }
// TODO set this back to the default noise
		Log.i(AlarmPublisher.class.getName(),"Scheduling test fetch");
		intent = new Intent(context, AlarmFetchReceiver.class);
		intent.putExtra("wakeupTime", wakeupTime);
		pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000L, pendingIntent);
	}

	/**
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
