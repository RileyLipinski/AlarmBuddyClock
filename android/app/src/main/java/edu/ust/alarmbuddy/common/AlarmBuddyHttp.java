package edu.ust.alarmbuddy.common;

import android.os.Build;
import android.util.Log;
import androidx.annotation.RequiresApi;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
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
//    private static final String DRIVER = "com.mysql.jdbc.Driver";
//    private static final String URL = "jdbc:mysql://34.122.120.203";
//
//    private static final String USER = "android";
//    private static final String PASS = ";pR$-fM]s4-F?2V%";

	public static final String LOCAL_SERVER_URL = "http://10.0.2.2:8080";

	private static final OkHttpClient client = new OkHttpClient();
    private static final String AB_RESTAPI = "https://alarmbuddy.wm.r.appspot.com/";
    public static final MediaType QUERYSTRING = MediaType.parse("application/x-www-form-urlencoded");

	public static void getLatestAlarmSound(Callback c) {
		Request r = new Request.Builder()
			.url(LOCAL_SERVER_URL + "/audio-test")
			.get()
			.build();
		System.out.println("Sending request to " + r.url().toString());
		client.newCall(r).enqueue(c);
	}

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static boolean authenticateLogin(String username, String password) throws IOException {

        //build the request
        String data = "username=" + username + "&password=" + password;
        URL url = new URL("https://alarmbuddy.wm.r.appspot.com/login");
        RequestBody body = RequestBody.create(data, QUERYSTRING);
        Request request = new Request.Builder()
                .url(url)
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

        // TODO replace with more robust token storage solution
        Log.i(AlarmBuddyHttp.class.getName(),"Writing token to file");
        File file = new File("/data/user/0/edu.ust.alarmbuddy/files/token");

        FileOutputStream outputStream = new FileOutputStream(file);
        outputStream.write(stringResponse[0].getBytes());
        outputStream.close();

        return stringResponse[0].substring(8,12).equals("true") && stringResponse[0] != null;
    }

    public static boolean createUser (String username, String password, String firstName, String lastName,
                                      String email, String phoneNumber, String birthdate)
    {
        String data = String.format("username=%s&password=%s&firstName=%s&lastName=%s&email=%s&phoneNumber=%s&birthDate=%s",
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

        return stringResponse[0] != null && stringResponse[0].substring(8,12).equals("true");

    }

}