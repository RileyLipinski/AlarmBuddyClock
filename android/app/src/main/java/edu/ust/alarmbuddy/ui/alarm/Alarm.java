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
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		Intent intent = new Intent(context, AlarmNoisemaker.class);

		/** intent.putExtra(String, value)
		 Needed for days of the week and name?
		 */

		PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context, alarmId, intent, 0);

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.HOUR_OF_DAY, hour);

		String alarmConfirmation = String
			.format("Alarm (%s) set for %s at %d:%d", name, calendar.get(Calendar.DAY_OF_WEEK),
				hour, minute);
		Toast toast = Toast.makeText(context, alarmConfirmation, Toast.LENGTH_LONG);
		toast.show();

		//alarmManager.RTC_WAKEUP: will wake up device if screen is off
		alarmManager
			.setExact(alarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmPendingIntent);
	}

}
