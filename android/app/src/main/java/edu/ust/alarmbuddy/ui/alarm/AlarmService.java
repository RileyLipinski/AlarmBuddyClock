package edu.ust.alarmbuddy.ui.alarm;

import static edu.ust.alarmbuddy.ui.alarm.App.CHANNEL_ID;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import edu.ust.alarmbuddy.AlarmActivity;
import edu.ust.alarmbuddy.R;


public class AlarmService extends Service {

	private MediaPlayer mediaPlayer;

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO verify that we can set multiple alarms

		Log.i(AlarmService.class.getName(), "Starting alarm from service");

		Intent alarmIntent = new Intent(this, AlarmActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, alarmIntent, 0);

		// create a MediaPlayer that plays the pre-alarm noise, then starts looping the actual alarm sound
		mediaPlayer = MediaPlayer.create(this, R.raw.pre_alarm_noise);
		mediaPlayer.setOnCompletionListener(x -> {
			mediaPlayer.release();
			mediaPlayer = null;
			String uriString = intent.getStringExtra("uri");

			if (uriString == null) {
				mediaPlayer = MediaPlayer.create(this, R.raw.alarm_buddy);
			} else {
				mediaPlayer = MediaPlayer.create(this, Uri.parse(uriString));
				if (mediaPlayer == null) {
					mediaPlayer = MediaPlayer.create(this, R.raw.alarm_buddy);
				}
			}
			mediaPlayer.setLooping(true);
			mediaPlayer.start();
		});

		Notification alarmNotification = new NotificationCompat.Builder(this, CHANNEL_ID)
			.setContentTitle("ALARM")
			.setContentText("RING RING RING")
			.setSmallIcon(R.drawable.ic_baseline_access_alarm_24)
			.setContentIntent(pendingIntent)
			.build();

		mediaPlayer.start();

		startForeground(1, alarmNotification);

		//String alarmName = String.format("%s Alarm", intent.getStringExtra(name));

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		mediaPlayer.stop();
		mediaPlayer.release();
		mediaPlayer = null;
		super.onDestroy();
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
