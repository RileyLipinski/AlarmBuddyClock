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

	private static final boolean PROD = false; // TODO delete on release

	/** The amount of time (millseconds) between polls for new sounds */
	public static final long INTERVAL = 60 * 1000;

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
			trigger(context);
		} else {
			switch (response.code()) {
				case 200:
					handle200(context, response);
					trigger(context);
					break;
				case 401: // user is no longer authenticated, stop polling
					Log.i(NotificationFetchReceiver.class.getName(),
						"User is logged out, not retrying");
					break;
				default:
					Log.w(NotificationFetchReceiver.class.getName(), String
						.format("Response code %d not explicitly handled, retrying",
							response.code()));
					trigger(context);
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
		Log.i(NotificationFetchReceiver.class.getName(),
			"Good response, checking for new sounds");
		String json;
		try {
			json = response.body().string();
			Log.i(NotificationFetchReceiver.class.getName(),
				"Response from server: " + json);
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
	 * Schedules an instance of this BroadcastReceiver to fire after NotificationFetchReceiver.INTERVAL
	 * milliseconds
	 *
	 * @param context Application context
	 */
	public static void trigger(Context context) {
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
			String url;

			if (PROD) {
				if (token == null) {
					// force API to return 401 if user has no credentials
					token = "";
					username = "username";
				}
				url = AlarmBuddyHttp.API_URL + "/sounds/" + username;
			} else { // TODO delete this if statement when releasing
				token = token == null ? "n" : "y";
				url = "http://10.0.2.2:8080/sounds-list/" + token;
			}

			Log.i(NotificationFetchReceiver.class.getName(), "Polling url " + url);

			Request request = new Request.Builder()
				.url(url)
				.header("Authorization", token)
				.get()
				.build();

			CountDownLatch latch = new CountDownLatch(1);
			new OkHttpClient().newCall(request).enqueue(new Callback() {
				@Override
				public void onFailure(@NotNull Call call, @NotNull IOException e) {
					Log.e(NotificationFetchReceiver.class.getName(), "Failed request to " + url);
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