package edu.ust.alarmbuddy.common;

import edu.ust.alarmbuddy.ui.alarm.AlarmSound;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

/**
 * Tutorial: https://www.journaldev.com/13629/okhttp-android-example-tutorial
 */
public class AlarmBuddyHttp {
//    private static final String DRIVER = "com.mysql.jdbc.Driver";
//    private static final String URL = "jdbc:mysql://34.122.120.203";
//
//    private static final String USER = "android";
//    private static final String PASS = ";pR$-fM]s4-F?2V%";

	private static final String LOCAL_SERVER_URL = "http://10.0.2.2:8080";

	private static final OkHttpClient client = new OkHttpClient();

	public static void getLatestAlarmSound(Callback c) {
		Request r = new Request.Builder()
			.url(LOCAL_SERVER_URL + "/audio-test")
			.get()
			.build();
		System.out.println("Sending request to " + r.url().toString());
		client.newCall(r).enqueue(c);
	}

	public static void getLatestAlarmSound(AlarmSound sound) {
		Request r = new Request.Builder()
			.url(LOCAL_SERVER_URL + "/audio-test")
			.get()
			.build();
		System.out.println("Sending request to " + r.url().toString());
		client.newCall(r).enqueue(new Callback() {
			@Override
			public void onFailure(@NotNull Call call, @NotNull IOException e) {
				call.cancel();
				sound.setFetched(true);
			}

			@Override
			public void onResponse(@NotNull Call call, @NotNull Response response)
				throws IOException {
				sound.setUri("Fetched");
				sound.setFetched(true);
			}
		});
	}
}
