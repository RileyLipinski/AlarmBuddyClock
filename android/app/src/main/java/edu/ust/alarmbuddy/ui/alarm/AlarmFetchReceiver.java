package edu.ust.alarmbuddy.ui.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import edu.ust.alarmbuddy.common.AlarmBuddyHttp;
import java.io.File;
import java.io.FileOutputStream;
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

		if (wakeupTime < System.currentTimeMillis()) {
			throw new RuntimeException(
				"Tried to schedule an alarm for earlier than the current time");
		}

		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder()
			.url(AlarmBuddyHttp.LOCAL_SERVER_URL + "/audio-test")
			.get()
			.build();

		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onResponse(@NotNull Call call, @NotNull Response response)
				throws IOException {
				File file = new File(context.getFilesDir(),"databaseAlarm.mp3");
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
				Log.i(AlarmFetchReceiver.class.getName(),"Scheduling alarm.");

				Intent outputIntent = new Intent(context,AlarmNoisemaker.class);
				outputIntent.putExtra("useDefaultNoise",useDefaultNoise);

				PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, outputIntent, 0);
				AlarmManager alarmManager = AlarmPublisher.getAlarmManager(context);
				alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000L, pendingIntent);
// TODO need to revise this to set alarms for the true time
				//				alarmManager.setExact(AlarmManager.RTC_WAKEUP, wakeupTime, pendingIntent);
			}
		});

	}
}
