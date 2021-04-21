package edu.ust.alarmbuddy.common;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.webkit.MimeTypeMap;
import androidx.annotation.RequiresApi;

import java.io.*;
import java.util.concurrent.CountDownLatch;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import edu.ust.alarmbuddy.R;
import okhttp3.*;

/**
 * Tutorial: https://www.journaldev.com/13629/okhttp-android-example-tutorial
 */
public class AlarmBuddyHttp {
//    private static final String DRIVER = "com.mysql.jdbc.Driver";
//    private static final String URL = "jdbc:mysql://34.122.120.203";
//
//    private static final String USER = "android";
//    private static final String PASS = ";pR$-fM]s4-F?2V%";

	public static final String LOCAL_SERVER_URL = "http://10.0.2.2:8080";

	public static final OkHttpClient client = new OkHttpClient();
	private static final String AB_RESTAPI = "https://alarmbuddy.wm.r.appspot.com/";
	public static final MediaType QUERYSTRING = MediaType
		.parse("application/x-www-form-urlencoded");

	public static void getLatestAlarmSound(Callback c) {
		Request r = new Request.Builder()
			.url(LOCAL_SERVER_URL + "/audio-test")
			.get()
			.build();
		System.out.println("Sending request to " + r.url().toString());
		client.newCall(r).enqueue(c);
	}

	@RequiresApi(api = Build.VERSION_CODES.KITKAT)

	public static boolean createUser(String username, String password, String firstName,
		String lastName,
		String email, String phoneNumber, String birthdate) {
		String data = String.format(
			"username=%s&password=%s&firstName=%s&lastName=%s&email=%s&phoneNumber=%s&birthDate=%s",
			username, password, firstName, lastName, email, phoneNumber, birthdate);
		RequestBody body = RequestBody.create(data, QUERYSTRING);
		Request request = new Request.Builder()
			.url("https://alarmbuddy.wm.r.appspot.com/register")
			.post(body)
			.build();

		//execute the request and wait for a response
		final String[] stringResponse = new String[1];
		final CountDownLatch latch = new CountDownLatch(1);
		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				call.cancel();
				latch.countDown();
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				stringResponse[0] = response.body().string();
				latch.countDown();
			}
		});
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return stringResponse[0] != null && stringResponse[0].substring(8, 12).equals("true");

	}
/*
	public static boolean uploadSound(Context context, String sound_path) {

		try {
			sound_path = "file:///android_asset/multiply.mp3"; //"file:///edu.ust.alarmbuddy/raw/multiply";
		}
		catch (Exception e) {
			Log.e("Upload Sound", "sound path " + e);
		}

		String mime = getMimeType(sound_path);

		Log.i("Upload Sound", sound_path + "  " + mime);

		String username = "";
		String token = "";
		try {
			username = UserData.getString(context, "username");
			token = UserData.getString(context, "token");
		} catch (Exception e) {
			Log.e("Upload Sound", "Could not retrieve username or token");
		}

		RequestBody body = new MultipartBody.Builder()
				.setType(MultipartBody.FORM)
				.addFormDataPart("file", "file:/" + "/" + "/android_asset/multiply.mp3",
						RequestBody.create(MediaType.parse(mime),
								new File(sound_path)))
				.build();

		//RequestBody body = RequestBody.create(data, QUERYSTRING);
		Request request = new Request.Builder()
				.url("https://alarmbuddy.wm.r.appspot.com/upload/" + username)
				.header("Authorization", token)
				.post(body)
				.build();

		Log.i("Upload Sound", request.toString());
		Log.i("Upload Sound", body.toString());

		//execute the request and wait for a response
		final String[] stringResponse = new String[1];
		final CountDownLatch latch = new CountDownLatch(1);
		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				call.cancel();
				latch.countDown();
				Log.i("call request", "onFailure");
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				stringResponse[0] = response.body().string();
				latch.countDown();
				Log.i("call request", "onResponse");
			}
		});
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}


		Call call = client.newCall(request);
		try {
			Response response = call.execute();
			Log.i("Upload Sound", response.toString());
		}
		catch(IOException e) {
			Log.e("Execute Call", "IOException" + e);
		}



		return stringResponse[0] != null && stringResponse[0].substring(8, 12).equals("true");

		return false;
	}

	public static String getMimeType(String url) {
		String type = null;
		String extension = MimeTypeMap.getFileExtensionFromUrl(url);
		if (extension != null) {
			type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
		}
		return type;
	}
*/

}
