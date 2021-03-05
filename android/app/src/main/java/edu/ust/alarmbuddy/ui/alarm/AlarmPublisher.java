package edu.ust.alarmbuddy.ui.alarm;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import java.util.Date;

public class AlarmPublisher extends BroadcastReceiver {

	public static String NOTIFICATION_ID = "notification_id";
	public static String NOTIFICATION = "notification";

	@Override
	public void onReceive(Context context, Intent intent) {
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		Notification notification = intent.getParcelableExtra(NOTIFICATION);
		int notificationId = intent.getIntExtra(NOTIFICATION_ID, 0);
		notificationManager.notify(notificationId, notification);

		System.out.println("Playing alarm at exact time " + new Date().toString());
	}
}
