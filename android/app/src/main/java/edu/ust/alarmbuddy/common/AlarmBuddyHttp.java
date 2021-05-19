package edu.ust.alarmbuddy.common;

import android.os.Build;
import android.util.Log;
import androidx.annotation.RequiresApi;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Tutorial: https://www.journaldev.com/13629/okhttp-android-example-tutorial
 */
public class AlarmBuddyHttp {

	public static final String API_URL = "https://alarmbuddy-312620.uc.r.appspot.com";

	public static final OkHttpClient client = new OkHttpClient();
	public static final MediaType QUERYSTRING = MediaType
		.parse("application/x-www-form-urlencoded");

	/**
	 * Makes a call to the database to create a new user.
	 *
	 * @param username    selected username
	 * @param password    selected password
	 * @param firstName   user's first name
	 * @param lastName    user's last name
	 * @param email       user's email
	 * @param phoneNumber user's phone number in xxx-xxx-xxxx format
	 * @param birthdate   user's birthdate in MM-DD-YYYY format
	 *
	 * @return whether or not user was successfully created
	 */
	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	public static boolean createUser(String username, String password, String firstName,
		String lastName, String email, String phoneNumber, String birthdate) {
		String data = String.format(
			"username=%s&password=%s&firstName=%s&lastName=%s&email=%s&phoneNumber=%s&birthDate=%s",
			username, password, firstName, lastName, email, phoneNumber, birthdate);
		RequestBody body = RequestBody.create(data, QUERYSTRING);
		Request request = new Request.Builder()
			.url(API_URL + "/register")
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
				Log.e("Create User", "Failure " + e);
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				stringResponse[0] = response.body().string();
				latch.countDown();
				Log.i("Create User", "Response " + stringResponse[0]);
			}
		});
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return stringResponse[0] != null && stringResponse[0].substring(8, 12).equals("true");

	}

}
