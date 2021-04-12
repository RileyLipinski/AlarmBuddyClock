package edu.ust.alarmbuddy.ui.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

public class AlarmFetchReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(getClass().toString(), "Fetching alarm noise from database");

		long wakeupTime = intent.getLongExtra("wakeupTime", Long.MIN_VALUE);

//		if (wakeupTime < System.currentTimeMillis()) {
//			throw new RuntimeException(
//				"Tried to schedule an alarm for earlier than the current time");
//		}

		OkHttpClient client = new OkHttpClient();
		// TODO remove hard-coded url in production
		Request request = new Request.Builder()
			.url("https://alarmbuddy.wm.r.appspot.com/download/johnny/erokia.wav")
			.header("Authorization", getToken(context))
			.get()
			.build();

		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onResponse(@NotNull Call call, @NotNull Response response)
				throws IOException {
				// TODO add error check on file type header
				File file = new File(context.getExternalFilesDir(""), "databaseAlarm.wav");
				byte[] responseBytes = response.body().bytes();

				FileOutputStream outputStream = new FileOutputStream(file);
				outputStream.write(responseBytes);
				outputStream.close();

				Log.i(AlarmFetchReceiver.class.getName(),
					"File successsfully downloaded from database: " + file.getAbsolutePath());

				scheduleAlarm(false);
			}

			@Override
			public void onFailure(@NotNull Call call, @NotNull IOException e) {
				call.cancel();
				Log.e(AlarmFetchReceiver.class.getName(),
					"Error fetching alarm from database, assigning default noise");
				scheduleAlarm(true);
			}

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

	public static String getToken(Context context) {
		// TODO remove hard-coded token file once storage solution is settled
		String jsonString;
		try {
			File file = new File(context.getExternalFilesDir(""), "token");
			BufferedReader reader = new BufferedReader(new FileReader(file));
			jsonString = reader.readLine();
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Error reading file in AlarmFetchReceiver");
		} catch (IOException e) {
			throw new RuntimeException("Error reading file in AlarmFetchReceiver");
		}

		if (jsonString != null) {
			JsonElement json = JsonParser.parseString(jsonString);
			String token = json.getAsJsonObject()
				.get("token")
				.getAsString();
			return token;
		}
		return null;
	}
}
