package edu.ust.alarmbuddy.worker.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import edu.ust.alarmbuddy.common.UserData;
import edu.ust.alarmbuddy.ui.alarm.AlarmPublisher;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

public class AlarmFetchReceiver extends BroadcastReceiver {

	/**
	 * Fetches an alarm sound from the database and schedules job to make noise and alert the user
	 *
	 * @param context The application context
	 * @param intent  Android Intent parameters
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(getClass().toString(), "Fetching alarm noise from database");

		long wakeupTime = intent.getLongExtra("wakeupTime", Long.MIN_VALUE);

		String token = "";
		try {
			token = UserData.getString(context, "token");
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String username = "";
		try {
			username = UserData.getString(context, "username");
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String soundId = "58";
		//TODO remove hardcoded sound id in production

		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder()
			.url(String
				.format("https://alarmbuddy.wm.r.appspot.com/download/%s/%s", username, soundId))
			.header("Authorization", token)
			.get()
			.build();

		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onResponse(@NotNull Call call, @NotNull Response response)
				throws IOException {
				//TODO handle non-200 response codes in here

				// error check on Content-Type response header
				String mimeType = response.header("Content-Type");

				if (mimeType != null && mimeType.equals("audio/mpeg")) {
					// response contained a proper audio file
					File file = new File(context.getExternalFilesDir(""), "databaseAlarm.mp3");
					byte[] responseBytes = response.body().bytes();

					FileOutputStream outputStream = new FileOutputStream(file);
					outputStream.write(responseBytes);
					outputStream.flush();
					outputStream.close();

					Log.i(AlarmFetchReceiver.class.getName(),
						"File successfully downloaded from database: " + file.getAbsolutePath());

					scheduleAlarm(false);
				} else {
					// response came back with a disallowed file type
					Log.e(AlarmFetchReceiver.class.getName(), "Mime type " + mimeType
						+ " is not permitted. Only audio/mpeg is permitted.");
					Log.e(AlarmFetchReceiver.class.getName(),
						"RESPONSE: \n" + response.body().string());
					scheduleAlarm(true);
				}
			}

			@Override
			public void onFailure(@NotNull Call call, @NotNull IOException e) {
				call.cancel();
				Log.e(AlarmFetchReceiver.class.getName(),
					"Error fetching alarm from database, assigning default noise");
				scheduleAlarm(true);
			}

			/**
			 * @param useDefaultNoise Determines whether alarm will be scheduled using the
			 *                        default noise
			 */
			private void scheduleAlarm(boolean useDefaultNoise) {
				Log.i(AlarmFetchReceiver.class.getName(),
					"Scheduling alarm. Default: " + useDefaultNoise);

				Intent outputIntent = new Intent(context, AlarmNoisemaker.class);
				intent.replaceExtras(new Bundle());
				outputIntent.putExtra("useDefaultNoise", useDefaultNoise);

				PendingIntent pendingIntent = PendingIntent
					.getBroadcast(context, 0, outputIntent, 0);
				AlarmManager alarmManager = AlarmPublisher.getAlarmManager(context);
				alarmManager.setExact(AlarmManager.RTC_WAKEUP, wakeupTime, pendingIntent);
			}
		});
	}
}
