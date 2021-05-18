package edu.ust.alarmbuddy.worker.notification;

import static edu.ust.alarmbuddy.ui.alarm.App.CHANNEL_ID;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import edu.ust.alarmbuddy.R;
import edu.ust.alarmbuddy.SoundListActivity;
import edu.ust.alarmbuddy.common.AlarmBuddyHttp;
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

	private static final String TAG = NotificationFetchReceiver.class.getName();

	/** The amount of time between polls for new sounds */
	public static final long INTERVAL = 6000000L;

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
			Log.e(TAG, "Could not get a response from database, retrying fetch");
			scheduleNotificationFetch(context);
		} else {
			switch (response.code()) {
				case 200:
					handle200(context, response);
					scheduleNotificationFetch(context);
					break;
				case 401: // user is no longer authenticated, stop polling
					Log.i(TAG, "User is logged out, not retrying");
					break;
				case 500:
					if (handle500(response)) {
						scheduleNotificationFetch(context);
					}
					break;
				default:
					Log.w(TAG, String.format("Response code %d not explicitly handled, retrying",
						response.code()));
					scheduleNotificationFetch(context);
			}
		}
	}

	/**
	 * Checks the response JSON to see if there are any new sounds in the DB. If new sounds exist,
	 * sends a notification to the user.
	 *
	 * @param context  Application context
	 * @param response The JSON response body from the server
	 */
	private void handle200(Context context, Response response) {
		Log.i(TAG, "Good response, checking for new sounds");
		String json;
		try {
			json = response.body().string();
//			Log.i(TAG, "Response from server: " + json);
		} catch (IOException e) {
			e.printStackTrace();
			json = "";
		}

		JsonArray soundsToNotify = getSoundsToNotify(context, json);
		if (soundsToNotify.size() > 0) {
			newSoundNotification(context, soundsToNotify);
		}
	}

	/**
	 * Determines whether the server returned 500 due to an expired API key or not. If it did,
	 * returns false to prevent rescheduling new notification fetches.
	 *
	 * @param response The response received from the database
	 *
	 * @return whether the app should schedule another poll job
	 */
	private boolean handle500(Response response) {
		String responseBody = "UNABLE TO RETRIEVE RESPONSE BODY";

		try {
			responseBody = response.body().string();

			boolean result = JsonParser.parseString(responseBody)
				.getAsJsonObject()
				.get("auth")
				.getAsBoolean();

			if (!result) {
				Log.i(TAG, "Server returned 500 due to expired token, not retrying.");
				return false;
			}
		} catch (Exception ignored) {
			// if an exception is thrown, then the response body does not match what is expected
			// when the user's API key is expired
		}

		Log.e(TAG, "Server returned 500 for reason other than expired token, retrying.");
		Log.e(TAG, responseBody);
		return true;
	}

	/**
	 * Schedules an instance of this BroadcastReceiver to fire after NotificationFetchReceiver.INTERVAL
	 * milliseconds
	 *
	 * @param context Application context
	 */
	public static void scheduleNotificationFetch(Context context) {
		AlarmPublisher.getAlarmManager(context).set(AlarmManager.ELAPSED_REALTIME_WAKEUP, INTERVAL,
			PendingIntent
				.getBroadcast(context, 0, new Intent(context, NotificationFetchReceiver.class),
					PendingIntent.FLAG_CANCEL_CURRENT));
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
				// force API to return 401 if user has no credentials
				token = "";
				username = "username";
			}
			String url = AlarmBuddyHttp.API_URL + "/sounds/" + username;

			Log.i(TAG, "Polling url " + url);

			Request request = new Request.Builder()
				.url(url)
				.header("Authorization", token)
				.get()
				.build();

			CountDownLatch latch = new CountDownLatch(1);
			new OkHttpClient().newCall(request).enqueue(new Callback() {
				@Override
				public void onFailure(@NotNull Call call, @NotNull IOException e) {
					Log.e(TAG, "Failed request to " + url);
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
		} catch (InterruptedException e) {
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
	private JsonArray getSoundsToNotify(Context context, @NonNull String response) {
		try {
			JsonArray result = new JsonArray();
			JsonArray json = JsonParser.parseString(response)
				.getAsJsonArray();

			int maxIdSeen = UserData.getInt(context, "maxIdSeen", Integer.MIN_VALUE);
			int maxIdInResponse = Integer.MIN_VALUE;

			for (JsonElement x : json) {
				int soundId = x.getAsJsonObject().get("soundID").getAsInt();

				if (soundId > maxIdSeen) {
					maxIdInResponse = soundId;
					result.add(x);
				}
			}

			if (maxIdInResponse > maxIdSeen) {
				UserData.getSharedPreferences(context).edit()
					.putInt("maxIdSeen", maxIdInResponse)
					.apply();
			}

			return result;
		} catch (GeneralSecurityException | IOException e) {
			e.printStackTrace();
			return new JsonArray();
		}
	}

	/**
	 * Sets up and sends a notification to the user when they have received new sounds from their
	 * friends
	 *
	 * @param context Application context
	 * @param json    The json string received from the sound list endpoint
	 */
	private void newSoundNotification(Context context, JsonArray json) {
		Intent intent = new Intent(context, SoundListActivity.class);
		intent.putExtra("json", json.toString());

		PendingIntent pendingIntent = PendingIntent
			.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
			.setContentTitle("You received a sound!")
			.setContentText(
				"MaxIdSeen: " + UserData.getInt(context, "maxIdSeen", Integer.MIN_VALUE))
			.setSmallIcon(R.drawable.ic_baseline_access_alarm_24)
			.setContentIntent(pendingIntent)
			.build();

		NotificationManager mNotificationManager = (NotificationManager) context
			.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(0, notification);
	}
}
