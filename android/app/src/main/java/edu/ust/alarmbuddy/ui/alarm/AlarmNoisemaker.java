package edu.ust.alarmbuddy.ui.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import edu.ust.alarmbuddy.R;
import java.io.File;
import java.io.IOException;
import java.util.Date;

public class AlarmNoisemaker extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(AlarmNoisemaker.class.getName(), "Playing noise at " + new Date().toString());
		if(intent.getBooleanExtra("useDefaultNoise",true)) {
			makeDefaultNoise(context);
		} else {
			// TODO base the file name on the file type being downloaded
			makeNoise(context, Uri.fromFile(new File(context.getFilesDir(),"databaseAlarm.wav")));
		}
	}

	public static void makeNoise(Context context, Uri uri) {
		try {
			MediaPlayer mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioAttributes(
				new AudioAttributes.Builder()
					.setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
					.setUsage(AudioAttributes.USAGE_MEDIA)
					.build()
			);
			mediaPlayer.setDataSource(context, uri);
			mediaPlayer.prepare();
			mediaPlayer.start();
		} catch (IOException e) {
			Log.e(AlarmNoisemaker.class.getName(),"NOISEMAKER FAILED");
		}
	}

	public static void makeDefaultNoise(Context context) {
		makeNoise(context, Uri.parse("android.resource://edu.ust.alarmbuddy/" + R.raw.alarm_buddy));
	}
}