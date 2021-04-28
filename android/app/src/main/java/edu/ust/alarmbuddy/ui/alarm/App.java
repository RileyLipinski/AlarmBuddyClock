package edu.ust.alarmbuddy.ui.alarm;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends Application {

	public static final String CHANNEL_ID = "AlarmServiceChannel";

	@Override
	public void onCreate() {
		super.onCreate();
		createNotificationChannel();
	}

	public void createNotificationChannel() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
