package edu.ust.alarmbuddy.ui.alarm;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import edu.ust.alarmbuddy.R;

public class App extends Application {

	public static final String CHANNEL_ID = "AlarmServiceChannel";

	/**
	 * This method creates the notification channel.
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		createNotificationChannel();
	}

	/**
	 * This method is modified due to a difference in Android 8.0.  If API is level 26, the
	 * notification must specify a notification channel.  Otherwise the notification will never
	 * appear and instead the system will log an error.
	 */
	public void createNotificationChannel() {
		//TODO notification channel not being created with pre-alarm sound
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel notificationChannel = new NotificationChannel(
				CHANNEL_ID,
				"Alarm Service Channel",
				NotificationManager.IMPORTANCE_DEFAULT
			);
			Log.i(App.class.getName(), "Creating notification channel");
			notificationChannel.setSound(
				Uri.parse("android.resource://" + getPackageName() + '/' + R.raw.pre_alarm_noise),
				new AudioAttributes.Builder().build()
			);
			NotificationManager notificationManager = getSystemService(NotificationManager.class);
			notificationManager.createNotificationChannel(notificationChannel);
		}
	}
}
