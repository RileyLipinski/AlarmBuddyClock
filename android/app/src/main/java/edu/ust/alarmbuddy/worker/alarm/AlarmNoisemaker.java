package edu.ust.alarmbuddy.worker.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import edu.ust.alarmbuddy.R;
import java.io.File;
import java.util.Date;

public class AlarmNoisemaker extends BroadcastReceiver {

	/**
	 * Plays the alarm sound downloaded by AlarmFetchReceiver, or the default noise if the download
	 * was not completed successfully.
	 *
	 * @param context Application context
	 * @param intent  Received Android Intent
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(AlarmNoisemaker.class.getName(), "Playing noise at " + new Date());
		Log.i(AlarmNoisemaker.class.getName(),
			"Default noise: " + intent.getBooleanExtra("useDefaultNoise", true));
		if (intent.getBooleanExtra("useDefaultNoise", true)) {
			makeDefaultNoise(context);
		} else {
			makeNoise(context,
				Uri.fromFile(new File(context.getExternalFilesDir(""), "databaseAlarm.mp3")));
		}

		intent.removeExtra("useDefaultNoise");
	}

	/**
	 * Plays the audio file specified by the parameter Uri
	 *
	 * @param context Application context
	 * @param uri     Uri of file to be played
	 */
	public static void makeNoise(Context context, Uri uri) {
		Log.i(AlarmNoisemaker.class.getName(), "Making noise using file " + uri.getPath());
		Intent intentService = new Intent(context, AlarmService.class);
		intentService.replaceExtras(new Bundle());
		intentService.putExtra("uri", uri.toString());

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			context.startForegroundService(intentService);
		} else {
			context.startService(intentService);
		}
	}

	public static void makeDefaultNoise(Context context) {
		makeNoise(context, Uri.parse("android.resource://edu.ust.alarmbuddy/" + R.raw.alarm_buddy));
	}
}
