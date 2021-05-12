package edu.ust.alarmbuddy.worker.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import edu.ust.alarmbuddy.common.AlarmBuddyHttp;
import edu.ust.alarmbuddy.common.UserData;
import edu.ust.alarmbuddy.ui.alarm.AlarmPublisher;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.concurrent.CountDownLatch;
import java.util.stream.StreamSupport;
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

		String token = UserData.getStringNotNull(context, "token");
		String username = UserData.getStringNotNull(context, "username");
		int soundId = getSoundId(username, token);

		downloadSound(context, wakeupTime, username, token, soundId);
	}

	private int getSoundId(String username, String token) {
		final int[] result = new int[]{-1};
		String url = AlarmBuddyHttp.API_URL + "/sounds/" + username;
		Log.i(AlarmFetchReceiver.class.getName(), "Sending request to " + url);

		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder()
			.url(url)
			.header("Authorization", token)
			.get()
			.build();
		CountDownLatch latch = new CountDownLatch(1);

		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(@NotNull Call call, @NotNull IOException e) {
				call.cancel();
				logHttpError("No response when retrieving sound list");
				latch.countDown();
			}

			@Override
			public void onResponse(@NotNull Call call, @NotNull Response response)
				throws IOException {
				String responseBody = response.body().string();
				if (response.code() == 200) {
					JsonArray json = JsonParser.parseString(responseBody).getAsJsonArray();

					// find the largest soundId in the json response body
					StreamSupport.stream(json.spliterator(), false)
						.map(x -> x.getAsJsonObject().get("soundID").getAsInt())
						.max(Comparator.comparing(Integer::valueOf))
						.ifPresent(integer -> result[0] = integer);
				} else {
					logHttpError("Sound list responded with non-success code " + response.code());
					logHttpError(responseBody);
				}
				latch.countDown();
			}
		});

		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return result[0];
	}

	private void downloadSound(Context context, long wakeupTime, String username, String token,
		int soundId) {
		String url = AlarmBuddyHttp.API_URL + "/download/" + username + "/" + soundId;
		Log.i(AlarmFetchReceiver.class.getName(), "Sending request to " + url);

		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder()
			.url(url)
			.header("Authorization", token)
			.get()
			.build();

		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onResponse(@NotNull Call call, @NotNull Response response)
				throws IOException {
				boolean useDefaultNoise = true;
				String mimeType = response.header("Content-Type");

				if (response.code() != 200) {
					logHttpError("Response came back with non-success code " + response.code());
					logHttpError(response.body().string());

				} else if (mimeType == null) {
					logHttpError("MIME type of response is null");
					logHttpError(response.body().string());

				} else if (!mimeType.equals("audio/mpeg")) {
					logHttpError(
						"MIME type " + mimeType + " is not permitted. Only audio/mpeg is allowed.");
					logHttpError(response.body().string());

				} else {
					saveDownloadedSound(context, response.body().bytes());
					useDefaultNoise = false;
				}

				scheduleAlarm(context, wakeupTime, useDefaultNoise);
			}

			@Override
			public void onFailure(@NotNull Call call, @NotNull IOException e) {
				call.cancel();
				Log.e(AlarmFetchReceiver.class.getName(),
					"No response when downloading sound from database");
				scheduleAlarm(context, wakeupTime, true);
			}
		});
	}

	private void saveDownloadedSound(Context context, byte[] bytes) throws IOException {
		File file = new File(context.getExternalFilesDir(""), "databaseAlarm.mp3");

		FileOutputStream outputStream = new FileOutputStream(file);
		outputStream.write(bytes);
		outputStream.flush();
		outputStream.close();

		Log.i(AlarmFetchReceiver.class.getName(),
			"File successfully downloaded from database: " + file.getAbsolutePath());
	}

	private void scheduleAlarm(Context context, long wakeupTime, boolean useDefaultNoise) {
		Log.i(AlarmFetchReceiver.class.getName(),
			"Scheduling alarm. Default: " + useDefaultNoise);

		Intent intent = new Intent(context, AlarmNoisemaker.class);
		intent.replaceExtras(new Bundle());
		intent.putExtra("useDefaultNoise", useDefaultNoise);

		PendingIntent pendingIntent = PendingIntent
			.getBroadcast(context, 0, intent, 0);
		AlarmManager alarmManager = AlarmPublisher.getAlarmManager(context);
		alarmManager.setExact(AlarmManager.RTC_WAKEUP, wakeupTime, pendingIntent);
	}

	private void logHttpError(String message) {
		Log.e(AlarmFetchReceiver.class.getName(), message);
	}
}
