package edu.ust.alarmbuddy.ui.alarms.database;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import edu.ust.alarmbuddy.ui.alarm.AlarmPublisher;
import edu.ust.alarmbuddy.worker.alarm.AlarmNoisemaker;

import java.util.Calendar;

@Entity(tableName = "alarms")
public class Alarm {
	@PrimaryKey
	@NonNull
	private int alarmId;
	private int hour;
	private int minute;
	private boolean sunday;
	private boolean monday;
	private boolean tuesday;
	private boolean wednesday;
	private boolean thursday;
	private boolean friday;
	private boolean saturday;
	private String name;
	private long created;

	public boolean isScheduled() {
		return scheduled;
	}

	private boolean scheduled;

	/**
	 * Object used to create an alarm
	 *
	 * @param alarmId   id used to identify alarm
	 * @param hour      hour alarm is set for
	 * @param minute    minute alarm is set for
	 * @param sunday    true if alarm should recur on Sundays
	 * @param monday    true if alarm should recur on Mondays
	 * @param tuesday   true if alarm should recur on Tuesdays
	 * @param wednesday true if alarm should recur on Wednesdays
	 * @param thursday  true if alarm should recur on Thursdays
	 * @param friday    true if alarm should recur on Fridays
	 * @param saturday  true if alarm should recur on Saturdays
	 * @param name      unique name given to alarm by user
	 * @param created   time alarm is created
	 * @param scheduled true if currently alarm service is currently scheduled
	 */
	public Alarm(int alarmId, int hour, int minute, boolean sunday, boolean monday, boolean tuesday,
		boolean wednesday, boolean thursday, boolean friday, boolean saturday, String name,
		long created, boolean scheduled) {
		this.alarmId = alarmId;
		this.hour = hour;
		this.minute = minute;
		this.sunday = sunday;
		this.monday = monday;
		this.tuesday = tuesday;
		this.wednesday = wednesday;
		this.thursday = thursday;
		this.friday = friday;
		this.saturday = saturday;
		this.name = name;
		this.created = created;
	}

	public void setAlarmId(int alarmId) {
		this.alarmId = alarmId;
	}

	public int getAlarmId() {
		return alarmId;
	}

	public int getHour() {
		return hour;
	}

	public int getMinute() {
		return minute;
	}

	public boolean isSunday() {
		return sunday;
	}

	public boolean isMonday() {
		return monday;
	}

	public boolean isTuesday() {
		return tuesday;
	}

	public boolean isWednesday() {
		return wednesday;
	}

	public boolean isThursday() {
		return thursday;
	}

	public boolean isFriday() {
		return friday;
	}

	public boolean isSaturday() {
		return saturday;
	}

	public String getName() {
		return name;
	}

	public long getCreated() {
		return created;
	}

	public void setCreated(long created) {
		this.created = created;
	}

	/**
	 * Method used to set an alarm
	 *
	 * @param context context of alarm being set
	 */
	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	public void setAlarm(Context context) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.HOUR_OF_DAY, hour);

		String dayOfWeek = "day";
		if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
			dayOfWeek = "Sunday";
		} else if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
			dayOfWeek = "Monday";
		} else if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY) {
			dayOfWeek = "Tuesday";
		} else if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) {
			dayOfWeek = "Wednesday";
		} else if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY) {
			dayOfWeek = "Thursday";
		} else if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
			dayOfWeek = "Friday";
		} else if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
			dayOfWeek = "Saturday";
		}

		String toastCreateAlarm;
		if(name.length() != 0) {
			toastCreateAlarm = String
					.format("Alarm (%s) set for %s at %02d:%02d", name, dayOfWeek, hour, minute);
		} else {
			toastCreateAlarm = String
					.format("Alarm set for %s at %02d:%02d", dayOfWeek, hour, minute);
		}

		Toast toast = Toast.makeText(context, toastCreateAlarm, Toast.LENGTH_LONG);
		toast.show();

		AlarmPublisher.publishAlarm(context, calendar.getTimeInMillis(), alarmId);
	}

	/**
	 * Method used to delete alarm
	 *
	 * @param context context of alarm being deleted
	 */
	public void deleteAlarm(Context context) {
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, AlarmNoisemaker.class);
		PendingIntent pendingIntent = PendingIntent
			.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		alarmManager.cancel(pendingIntent);

		String toastDeleteAlarm = String
			.format("Alarm (%s) deleted for %02d:%02d", name, hour, minute);
		Toast toast = Toast.makeText(context, toastDeleteAlarm, Toast.LENGTH_LONG);
		toast.show();
		Log.i("deleted", toastDeleteAlarm);
	}
}
