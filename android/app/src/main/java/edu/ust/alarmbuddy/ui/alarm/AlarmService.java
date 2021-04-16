package edu.ust.alarmbuddy.ui.alarm;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import edu.ust.alarmbuddy.AlarmActivity;
import edu.ust.alarmbuddy.R;

import static edu.ust.alarmbuddy.ui.alarm.App.CHANNEL_ID;

public class AlarmService extends Service {

	private MediaPlayer mediaPlayer;

	@Override
	public void onCreate() {
		super.onCreate();

		mediaPlayer = mediaPlayer.create(this, R.raw.alarm_buddy);
		mediaPlayer.setLooping(true);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Intent alarmIntent = new Intent(this, AlarmActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, alarmIntent, 0);

		//String alarmName = String.format("%s Alarm", intent.getStringExtra(name));

		Notification alarmNotification = new NotificationCompat.Builder(this, CHANNEL_ID)
			.setContentTitle("ALARM")
			.setContentText("RING RING RING")
			.setSmallIcon(R.drawable.ic_baseline_access_alarm_24)
			.setContentIntent(pendingIntent)
			.build();

		mediaPlayer.start();

		startForeground(1, alarmNotification);

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mediaPlayer.stop();
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
