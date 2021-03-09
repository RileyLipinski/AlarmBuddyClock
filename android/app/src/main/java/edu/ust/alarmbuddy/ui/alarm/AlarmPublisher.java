package edu.ust.alarmbuddy.ui.alarm;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import androidx.core.app.NotificationCompat;
import edu.ust.alarmbuddy.MainActivity;
import edu.ust.alarmbuddy.R;
import edu.ust.alarmbuddy.common.AlarmBuddyHttp;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;

public class AlarmPublisher extends BroadcastReceiver {

	public static String NOTIFICATION_ID = "notification_id";
	public static String NOTIFICATION = "notification";

	@Override
	public void onReceive(Context context, Intent intent) {
		NotificationManager notificationManager = (NotificationManager) context
			.getSystemService(Context.NOTIFICATION_SERVICE);

		Notification notification = intent.getParcelableExtra(NOTIFICATION);
		int notificationId = intent.getIntExtra(NOTIFICATION_ID, 0);
		notificationManager.notify(notificationId, notification);

		System.out.println("Playing alarm at exact time " + new Date().toString());
	}

	public static void publishAlarm(Context context, int notificationId, int hours, int minutes) {
		//TODO scheduled alarms are deleted if the device is turned off
		// see: https://stackoverflow.com/questions/36902667/how-to-schedule-notification-in-android
		// see: https://singhajit.com/schedule-local-notification-in-android/

		// TODO banner still isn't popping up

		// TODO alarm isn't annoying enough (i.e. doesn't repeat sound, goes away on its own)

		// TODO need to keep track of when alarms are set off

		long wakeupTime = wakeupTime(hours, minutes, System.currentTimeMillis());

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
			.setContentTitle("Alarm")
			.setContentText("Wake up")
			.setSmallIcon(R.drawable.ic_baseline_access_alarm_24);

		Intent intent = new Intent(context, MainActivity.class);
		PendingIntent activity = PendingIntent
			.getActivity(context, notificationId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		builder.setContentIntent(activity);

		AlarmBuddyHttp.getLatestAlarmSound(new Callback() {
			@Override
			public void onFailure(@NotNull Call call, @NotNull IOException e) {
				call.cancel();
				System.out.println("Request failed");
				builder.setSound(
					Uri.parse("android.resource://edu.ust.alarmbuddy/" + R.raw.alarm_buddy));
				dispatchAlarm();
			}

			@Override
			public void onResponse(@NotNull Call call, @NotNull Response response)
				throws IOException {
				if (response.code()
					== 200) { // got a sound from the DB, save and set as current sound
					ResponseBody body = response.body();
					File tempMp3 = File.createTempFile("alarmSound", "mp3");
					tempMp3.deleteOnExit();
					FileOutputStream fos = new FileOutputStream(tempMp3);
					fos.write(body.bytes());
					body.close();
					fos.close();

					Uri myUri = Uri.fromFile(tempMp3);
					MediaPlayer mediaPlayer = new MediaPlayer();
					mediaPlayer.setAudioAttributes(
						new AudioAttributes.Builder()
							.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
							.setUsage(AudioAttributes.USAGE_MEDIA)
							.build()
					);
					mediaPlayer.setDataSource(context, myUri);
					mediaPlayer.prepare();
					mediaPlayer.start();

					//TODO attach fetched sound to notification, instead of just playing immediately
					//TODO this approach might not work if user's phone is not connected to internet
					//is it acceptable to force the default sound when this is the case?
				}

				builder.setSound(
					Uri.parse("android.resource://edu.ust.alarmbuddy/" + R.raw.alarm_buddy));

				dispatchAlarm();
			}

			private void dispatchAlarm() {
				Notification notification = builder.build();

				Intent notificationIntent = new Intent(context, AlarmPublisher.class);
				notificationIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
				notificationIntent.putExtra(AlarmPublisher.NOTIFICATION_ID, notificationId);
				notificationIntent.putExtra(AlarmPublisher.NOTIFICATION, notification);
				PendingIntent pendingIntent = PendingIntent
					.getBroadcast(context, notificationId, notificationIntent,
						PendingIntent.FLAG_CANCEL_CURRENT);

				AlarmManager alarmManager = (AlarmManager) context
					.getSystemService(Context.ALARM_SERVICE);
				alarmManager.setExact(AlarmManager.RTC_WAKEUP, wakeupTime, pendingIntent);

				System.out.println("Set alarm for " + new Date(wakeupTime).toString());
			}
		});
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
			Calendar tomorrowAtTime = (Calendar) todayAtTime.clone();
			tomorrowAtTime.roll(Calendar.DAY_OF_YEAR, 1);
			if (tomorrowAtTime.before(todayAtTime)) {
				// will occur on Dec 31, cycles back to Jan 1 without incrementing year
				tomorrowAtTime.roll(Calendar.YEAR, 1);
			}
			return tomorrowAtTime.getTimeInMillis();
		}
	}
}
