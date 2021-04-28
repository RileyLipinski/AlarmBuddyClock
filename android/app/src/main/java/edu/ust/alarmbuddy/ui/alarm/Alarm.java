package edu.ust.alarmbuddy.ui.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import java.util.Calendar;

public class Alarm {

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

	public Alarm(int alarmId, int hour, int minute, boolean sunday, boolean monday, boolean tuesday,
		boolean wednesday,
		boolean thursday, boolean friday, boolean saturday, String name, long created) {
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

	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	public void setAlarm(Context context) {
//		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//
//		Intent intent = new Intent(context, AlarmPublisher.class);
//
//		PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context, alarmId, intent, 0);

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.HOUR_OF_DAY, hour);

		String dayOfWeek = "day";
		if(calendar.get(Calendar.DAY_OF_WEEK) == 1) {
			dayOfWeek = "Sunday";
		} else if (calendar.get(Calendar.DAY_OF_WEEK) == 2) {
			dayOfWeek = "Monday";
		} else if (calendar.get(Calendar.DAY_OF_WEEK) == 3) {
			dayOfWeek = "Tuesday";
		} else if (calendar.get(Calendar.DAY_OF_WEEK) == 4) {
			dayOfWeek = "Wednesday";
		} else if (calendar.get(Calendar.DAY_OF_WEEK) == 5) {
			dayOfWeek = "Thursday";
		} else if (calendar.get(Calendar.DAY_OF_WEEK) == 6) {
			dayOfWeek = "Friday";
		} else if (calendar.get(Calendar.DAY_OF_WEEK) == 7) {
			dayOfWeek = "Saturday";
		}

		String alarmConfirmation = String
			.format("Alarm (%s) set for %s at %02d:%02d", name, dayOfWeek, hour, minute);
		Toast toast = Toast.makeText(context, alarmConfirmation, Toast.LENGTH_LONG);
		toast.show();

		AlarmPublisher.publishAlarm(context,calendar.getTimeInMillis());
	}

}
