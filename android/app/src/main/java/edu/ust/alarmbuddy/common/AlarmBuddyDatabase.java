package edu.ust.alarmbuddy.common;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Tutorial: https://www.journaldev.com/13629/okhttp-android-example-tutorial
 */
public class AlarmBuddyDatabase {
    private static final String DRIVER = "com.mysql.jdbc.Driver";
    private static final String URL = "jdbc:mysql://34.122.120.203";

    private static final String USER = "android";
    private static final String PASS = ";pR$-fM]s4-F?2V%";

    private static final OkHttpClient client = new OkHttpClient();

    /**
     * Test method that simply pings a web service as a proof of concept
     */

    public static void sendTestRequest(Callback callback) {
        Request r = new Request.Builder()
                .url("http://10.0.2.2:8080/json-test")
                .get()
                .build();
        client.newCall(r).enqueue(callback);
    }
}
