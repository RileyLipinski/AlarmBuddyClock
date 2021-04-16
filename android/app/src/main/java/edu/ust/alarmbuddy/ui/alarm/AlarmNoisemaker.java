package edu.ust.alarmbuddy.ui.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import edu.ust.alarmbuddy.R;
import java.io.File;
import java.io.IOException;
import java.util.Date;

public class AlarmNoisemaker extends BroadcastReceiver {

	private static MediaPlayer mediaPlayer;

	/**
	 * Plays the alarm sound set in the input Intent
	 *
	 * @param context Application context
	 * @param intent  Received Android Intent
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		if (mediaPlayer != null) {
			mediaPlayer.release();
		}

		Log.i(AlarmNoisemaker.class.getName(), "Playing noise at " + new Date().toString());
		Log.i(AlarmNoisemaker.class.getName(),
			"Default noise: " + intent.getBooleanExtra("useDefaultNoise", true));
		if (intent.getBooleanExtra("useDefaultNoise", true)) {
			makeDefaultNoise(context);
		} else {
			// TODO base the file name on the file type being downloaded
			makeNoise(context,
				Uri.fromFile(new File(context.getExternalFilesDir(""), "databaseAlarm.wav")));
		}

		intent.removeExtra("useDefaultNoise");
	}

	/**
	 * Plays an audio file
	 *
	 * @param context Application context
	 * @param uri     Uri of file to be played
	 */
	public static void makeNoise(Context context, Uri uri) {
		try {
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioAttributes(
				new AudioAttributes.Builder()
					.setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
					.setUsage(AudioAttributes.USAGE_MEDIA)
					.build()
			);
			mediaPlayer.setLooping(true);
			mediaPlayer.setDataSource(context, uri);
			mediaPlayer.prepare();
			//mediaPlayer.start();
			Intent intentService = new Intent(context, AlarmService.class);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				context.startForegroundService(intentService);
			} else {
				context.startService(intentService);
			}
		} catch (IOException e) {
			Log.e(AlarmNoisemaker.class.getName(), "NOISEMAKER FAILED");
		}
	}

	/**
	 * Plays the default audio file by calling makeNoise()
	 *
	 * @param context Application context
	 */
	public static void makeDefaultNoise(Context context) {
		makeNoise(context, Uri.parse("android.resource://edu.ust.alarmbuddy/" + R.raw.alarm_buddy));
	}
}
