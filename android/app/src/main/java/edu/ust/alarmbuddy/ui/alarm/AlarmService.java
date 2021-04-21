package edu.ust.alarmbuddy.ui.alarm;

import static edu.ust.alarmbuddy.ui.alarm.App.CHANNEL_ID;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import edu.ust.alarmbuddy.AlarmActivity;
import edu.ust.alarmbuddy.R;
import java.io.IOException;

public class AlarmService extends Service {

	private MediaPlayer mediaPlayer;

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//TODO add pre-alarm sound to this notification
		Log.i(AlarmService.class.getName(), "Starting alarm from service");

		Intent alarmIntent = new Intent(this, AlarmActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, alarmIntent, 0);

		if (mediaPlayer != null) {
			mediaPlayer.release();
		}
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setAudioAttributes(
			new AudioAttributes.Builder()
				.setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
				.setUsage(AudioAttributes.USAGE_MEDIA)
				.build()
		);
		mediaPlayer.setLooping(true);

		String uriString = intent.getStringExtra("uri");
		try {
			if (uriString == null) {
				mediaPlayer.setDataSource(this,
					Uri.parse("android.resource://edu.ust.alarmbuddy/" + R.raw.alarm_buddy));
			} else {
				mediaPlayer.setDataSource(this, Uri.parse(uriString));
			}
			mediaPlayer.prepare();
		} catch (IOException e) {
			// TODO implement this?
		}

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
		mediaPlayer.release();
		mediaPlayer = null;
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
