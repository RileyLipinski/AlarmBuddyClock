package edu.ust.alarmbuddy.ui.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import edu.ust.alarmbuddy.R;
import java.io.IOException;
import java.util.Date;

public class AlarmNoisemaker extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO allow for custom sounds here
		System.out.println("Playing noise at " + new Date().toString());
		makeDefaultNoise(context);
	}

	public static void makeDefaultNoise(Context context) {
		try {
			Uri myUri = Uri.parse("android.resource://edu.ust.alarmbuddy/" + R.raw.alarm_buddy);
			MediaPlayer mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioAttributes(
				new AudioAttributes.Builder()
					.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
					.setUsage(AudioAttributes.USAGE_MEDIA)
					.build()
			);
			mediaPlayer.setDataSource(context, myUri);
			mediaPlayer.prepare();
			mediaPlayer.start();
		} catch (IOException e) {
			System.out.println("NOISEMAKER FAILED");
		}
	}
}
