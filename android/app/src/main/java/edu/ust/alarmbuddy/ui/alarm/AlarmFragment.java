package edu.ust.alarmbuddy.ui.alarm;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import edu.ust.alarmbuddy.MainActivity;
import edu.ust.alarmbuddy.R;
import java.util.Calendar;
import java.util.Date;

public class AlarmFragment extends Fragment {

	private AlarmViewModel alarmViewModel;

	public View onCreateView(@NonNull LayoutInflater inflater,
		ViewGroup container, Bundle savedInstanceState) {
		alarmViewModel = new ViewModelProvider(this).get(AlarmViewModel.class);
		View root = inflater.inflate(R.layout.fragment_alarm, container, false);

		final TextView textView = root.findViewById(R.id.text_alarm);
		alarmViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

		// TODO change this to not require 24-hour time
		final Spinner alarmHours = root.findViewById(R.id.alarm_hours);
		ArrayAdapter<CharSequence> hoursAdapter = ArrayAdapter
			.createFromResource(getContext(), R.array.hours_array,
				android.R.layout.simple_spinner_item);
		hoursAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		alarmHours.setAdapter(hoursAdapter);

		final Spinner alarmMinutes = root.findViewById(R.id.alarm_minutes);
		ArrayAdapter<CharSequence> minutesAdapter = ArrayAdapter
			.createFromResource(getContext(), R.array.minutes_array,
				android.R.layout.simple_spinner_item);
		hoursAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		alarmMinutes.setAdapter(minutesAdapter);

		final Button button = root.findViewById(R.id.createAlarm);
		button.setOnClickListener(view -> {
			int hours = Integer.parseInt((String) alarmHours.getSelectedItem());
			int minutes = Integer.parseInt((String) alarmMinutes.getSelectedItem());

			scheduleAlarm((int) (System.currentTimeMillis() % 10000000L), hours, minutes);
		});
		return root;
	}

	public void scheduleAlarm(int notificationId, int hours, int minutes) {
		//TODO scheduled alarms are deleted if the device is turned off
		// see: https://stackoverflow.com/questions/36902667/how-to-schedule-notification-in-android
		// see: https://singhajit.com/schedule-local-notification-in-android/

		// TODO banner still isn't popping up

		// TODO alarm isn't annoying enough (i.e. doesn't repeat sound, goes away on its own)

		// TODO need to keep track of when alarms are set off

		Context context = getContext();
		long wakeupTime = wakeupTime(hours, minutes);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
			.setContentTitle("Alarm")
			.setContentText("Wake up")
			.setSmallIcon(R.drawable.ic_baseline_access_alarm_24)
			.setSound(getAlarmSound());

		Intent intent = new Intent(context, MainActivity.class);
		PendingIntent activity = PendingIntent
			.getActivity(context, notificationId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		builder.setContentIntent(activity);

		Notification notification = builder.build();

		Intent notificationIntent = new Intent(context, AlarmPublisher.class);
		notificationIntent.putExtra(AlarmPublisher.NOTIFICATION_ID, notificationId);
		notificationIntent.putExtra(AlarmPublisher.NOTIFICATION, notification);
		PendingIntent pendingIntent = PendingIntent
			.getBroadcast(context, notificationId, notificationIntent,
				PendingIntent.FLAG_CANCEL_CURRENT);

		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.setExact(AlarmManager.RTC_WAKEUP, wakeupTime, pendingIntent);

		System.out.println("Set alarm for " + new Date(wakeupTime).toString());
	}

	/**
	 * @param hours   The hours the alarm will be set for
	 * @param minutes The minute the alarm will be set for
	 *
	 * @return The System time (in milliseconds) when the alarm should go off
	 */
	public static long wakeupTime(int hours, int minutes) {
		return wakeupTime(hours, minutes, System.currentTimeMillis());
	}

	/**
	 * @param hours   The hours the alarm will be set for
	 * @param minutes The minute the alarm will be set for
	 * @param now     The current time, in milliseconds
	 *
	 * @return the time that the user alarm should play, relative to the system time
	 */
	public static long wakeupTime(int hours, int minutes, long now) {
		// TODO not playing on the precise second consistently
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
			// TODO not rolling over to next year correctly
			Calendar tomorrowAtTime = (Calendar) todayAtTime.clone();
			tomorrowAtTime.roll(Calendar.DAY_OF_YEAR, 1);
			if (tomorrowAtTime.before(todayAtTime)) {
				// will occur on Dec 31, cycles back to Jan 1 without incrementing year
				tomorrowAtTime.roll(Calendar.YEAR, 1);
			}
			return tomorrowAtTime.getTimeInMillis();
		}
	}

	/**
	 * Fetches the next queued custom alarm sound for the current user, or returns the default
	 * sound
	 *
	 * @return the alarm sound that will be applied to the next alarm notification
	 */
	private Uri getAlarmSound() {
		// TODO need to implement a DB fetch for the next alarm noise eventually
		//TODO might need to move this closer in time to the actual notification so that the
		// newest alarm tone can be fetched from the DB
		return Uri.parse("android.resource://edu.ust.alarmbuddy/" + R.raw.alarm_buddy);
	}
}