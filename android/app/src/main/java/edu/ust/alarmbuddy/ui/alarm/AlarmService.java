package edu.ust.alarmbuddy.ui.alarm;

import static edu.ust.alarmbuddy.ui.alarm.App.CHANNEL_ID;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
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

	/**
	 * This method is used to begin the alarm by sending a notification and starting the sound.
	 *
	 * @param intent
	 * @param flags
	 * @param startId
	 * @return
	 */
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

	/**
	 * This method is used to stop the alarm service.
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		mediaPlayer.stop();
		mediaPlayer.release();
		mediaPlayer = null;
	}

	/**
	 * Return the communication channel to the service.  May return null if
	 * clients can not bind to the service.  The returned
	 * {@link IBinder} is usually for a complex interface
	 * that has been <a href="{@docRoot}guide/components/aidl.html">described using
	 * aidl</a>.
	 *
	 * <p><em>Note that unlike other application components, calls on to the
	 * IBinder interface returned here may not happen on the main thread
	 * of the process</em>.  More information about the main thread can be found in
	 * <a href="{@docRoot}guide/topics/fundamentals/processes-and-threads.html">Processes and
	 * Threads</a>.</p>
	 *
	 * @param intent The Intent that was used to bind to this service,
	 *               as given to {@link Context#bindService
	 *               Context.bindService}.  Note that any extras that were included with
	 *               the Intent at that point will <em>not</em> be seen here.
	 * @return Return an IBinder through which clients can call on to the
	 * service.
	 */
	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
