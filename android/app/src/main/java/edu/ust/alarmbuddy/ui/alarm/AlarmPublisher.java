package edu.ust.alarmbuddy.ui.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import java.util.Calendar;

public class AlarmPublisher {

	public static int TWO_MINUTES = 2 * 60 * 1000;
	public static int TWENTY_FOUR_HOURS = 24 * 60 * 60 * 1000;

	public static void publishAlarm(Context context, int hours, int minutes) {
		//TODO add some kind of debug setting for the demo
			// possibly a new button in the UI
		long wakeupTime = wakeupTime(hours, minutes, System.currentTimeMillis());

		Intent intent;
		PendingIntent pendingIntent;
		AlarmManager alarmManager = getAlarmManager(context);

		if(wakeupTime - (TWO_MINUTES + 30000L) < System.currentTimeMillis()) {
			// set default alarm because there is not time to guarantee a successful fetch
		    Log.i(AlarmPublisher.class.getName(),"Setting default alarm");
		    intent = new Intent(context,AlarmNoisemaker.class);
		    pendingIntent = PendingIntent.getBroadcast(context,0,intent,0);
		    alarmManager.setExact(AlarmManager.RTC_WAKEUP,wakeupTime,pendingIntent);
        } else {
			Log.i(AlarmPublisher.class.getName(),"Scheduling alarm fetch");
			intent = new Intent(context, AlarmFetchReceiver.class);
			intent.putExtra("wakeupTime", wakeupTime);
			pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
			alarmManager.setExact(AlarmManager.RTC_WAKEUP, wakeupTime - TWO_MINUTES, pendingIntent);
        }
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
