package edu.ust.alarmbuddy.ui.alarm;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

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
	 * This method is modified due to a difference in Android 8.0.  If API is level 26, the notification must specify a notification channel.  Otherwise the notification will never appear and instead the system will log an error.
	 */
	public void createNotificationChannel() {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel notificationChannel = new NotificationChannel(
					CHANNEL_ID,
					"Alarm Service Channel",
					NotificationManager.IMPORTANCE_DEFAULT
			);

			NotificationManager notificationManager = getSystemService(NotificationManager.class);
			notificationManager.createNotificationChannel(notificationChannel);
		}
	}
}
