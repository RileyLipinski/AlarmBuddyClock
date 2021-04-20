package edu.ust.alarmbuddy.ui.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import edu.ust.alarmbuddy.common.UserData;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

public class AlarmFetchReceiver extends BroadcastReceiver {

	public static final String[] AUDIO_MIME_TYPES = {
		"audio/mpeg",
		"audio/wav"
	};

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

		OkHttpClient client = new OkHttpClient();
		// TODO remove hard-coded url in production
		Request request = new Request.Builder()
			.url("https://alarmbuddy.wm.r.appspot.com/download/johnny/erokia.wav")
			.header("Authorization", token)
			.get()
			.build();

		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onResponse(@NotNull Call call, @NotNull Response response)
				throws IOException {
				// error check on Content-Type response header
				String mimeType = response.header("Content-Type");
				if (mimeType == null || Arrays.binarySearch(AUDIO_MIME_TYPES, mimeType) < 0) {
					StringBuilder message = new StringBuilder(
						"Mime type " + mimeType + " is not permitted. Allowed types: ");
					for (String x : AUDIO_MIME_TYPES) {
						message.append(x).append(", ");
					}
					message.delete(message.length() - 2, message.length());
					throw new IllegalArgumentException(message.toString());
				}

				File file = new File(context.getExternalFilesDir(""), "databaseAlarm.wav");
				byte[] responseBytes = response.body().bytes();

				FileOutputStream outputStream = new FileOutputStream(file);
				outputStream.write(responseBytes);
				outputStream.close();

				Log.i(AlarmFetchReceiver.class.getName(),
					"File successfully downloaded from database: " + file.getAbsolutePath());

				scheduleAlarm(false);
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
				if (outputIntent.getExtras() != null && outputIntent.getExtras()
					.containsKey("useDefaultNoise")) {
					outputIntent.removeExtra("useDefaultNoise");
				}
				outputIntent.putExtra("useDefaultNoise", useDefaultNoise);

				PendingIntent pendingIntent = PendingIntent
					.getBroadcast(context, 0, outputIntent, 0);
				AlarmManager alarmManager = AlarmPublisher.getAlarmManager(context);
				alarmManager.setExact(AlarmManager.RTC_WAKEUP, wakeupTime, pendingIntent);
			}
		});

	}
}
