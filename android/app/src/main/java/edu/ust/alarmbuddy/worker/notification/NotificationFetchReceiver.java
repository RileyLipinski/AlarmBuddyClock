package edu.ust.alarmbuddy.worker.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import edu.ust.alarmbuddy.common.UserData;
import edu.ust.alarmbuddy.ui.alarm.AlarmPublisher;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.CountDownLatch;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

public class NotificationFetchReceiver extends BroadcastReceiver {

	public static final long INTERVAL = 10 * 1000;

	/**
	 * Upon receiving an Intent, polls the database for new sounds and takes the appropriate action
	 *
	 * @param context Application context
	 * @param intent  Received intent
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		Response response = pollForSounds(context);
		if (response == null) {
			Log.e(NotificationFetchReceiver.class.getName(),
				"Could not get a response from database, retrying fetch");
			triggerSelf(context);
		} else {
			switch (response.code()) {
				case 200: // check if new sounds exist and notify the user if so
					Log.i(NotificationFetchReceiver.class.getName(),
						"Good response, checking for new sounds");
					if (checkNotify(context, response)) {
						newSoundNotification(context);
					}
					triggerSelf(context);
					break;
				case 401: // user is no longer authenticated, stop polling
					Log.i(NotificationFetchReceiver.class.getName(),
						"User is logged out, not retrying");
					break;
				default:
					Log.i(NotificationFetchReceiver.class.getName(), String
						.format("Response code %d not explicitly handled, retrying",
							response.code()));
					triggerSelf(context);
			}
		}
	}

	/**
	 * Schedules an instance of this BroadcastReceiver to fire after the number of milliseconds
	 * specified in NotificationFetchReceiver.INTERVAL
	 *
	 * @param context Application context
	 */
	public static void triggerSelf(Context context) {
		AlarmPublisher.getAlarmManager(context).set(AlarmManager.ELAPSED_REALTIME_WAKEUP, INTERVAL,
			PendingIntent
				.getBroadcast(context, 0, new Intent(context, NotificationFetchReceiver.class), 0));
	}

	/**
	 * Requests the user's list of sounds from the DB.
	 *
	 * @param context Application context
	 *
	 * @return the Response received from the DB, or null if the request fails before receiving a
	 * response
	 */
	private static Response pollForSounds(Context context) {
		final Response[] result = new Response[1];

		try {
			String token = UserData.getString(context, "token");
			String username = UserData.getString(context, "username");
			if (token == null) {
				// credentials are not stored in SharedPreferences, probably due to user logout
				// force API to return 401
				token = "";
				username = "username";
			}

			Request request = new Request.Builder()
				.url("https://alarmbuddy.wm.r.appspot.com/sounds/" + username)
				.header("Authorization", token)
				.get()
				.build();

			CountDownLatch latch = new CountDownLatch(1);
			new OkHttpClient().newCall(request).enqueue(new Callback() {
				@Override
				public void onFailure(@NotNull Call call, @NotNull IOException e) {
					call.cancel();
					result[0] = null;
					latch.countDown();
				}

				@Override
				public void onResponse(@NotNull Call call, @NotNull Response response) {
					result[0] = response;
					latch.countDown();
				}
			});
			latch.await();
			return result[0];
		} catch (GeneralSecurityException | IOException | InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Determines whether the user has new sounds in the database.
	 *
	 * @param context  Application context
	 * @param response Response object containing user sound list
	 *
	 * @return whether the user must be notified of new sounds
	 */
	private static boolean checkNotify(Context context, @NonNull Response response) {
		try {
			JsonArray json = JsonParser.parseString(response.body().string())
				.getAsJsonArray();

			int maxIdSeen = UserData.getInt(context, "maxIdSeen", Integer.MIN_VALUE);
			int maxIdInResponse = Integer.MIN_VALUE;

			for (JsonElement x : json) {
				maxIdInResponse = Math
					.max(maxIdInResponse, x.getAsJsonObject().get("soundID").getAsInt());
			}
			if (maxIdInResponse > maxIdSeen) {
				UserData.getSharedPreferences(context).edit()
					.putInt("maxIdSeen", maxIdInResponse)
					.apply();
				return true;
			}
		} catch (GeneralSecurityException | IOException e) {
			e.printStackTrace();
			return false;
		}

		return false;
	}

	private static void newSoundNotification(Context context) {
		//TODO send an actual notification here
		Toast.makeText(context, "NEW SOUNDS", Toast.LENGTH_SHORT).show();
	}
}
