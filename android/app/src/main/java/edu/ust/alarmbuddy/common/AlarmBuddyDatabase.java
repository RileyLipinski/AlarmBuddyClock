package edu.ust.alarmbuddy.common;

import edu.ust.alarmbuddy.MainActivity;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Tutorial: https://www.journaldev.com/13629/okhttp-android-example-tutorial
 */
public class AlarmBuddyDatabase {
    private static final String DRIVER = "com.mysql.jdbc.Driver";
    private static final String URL = "jdbc:mysql://34.122.120.203";

    private static final String USER = "android";
    private static final String PASS = ";pR$-fM]s4-F?2V%";

    /**
     * Test method that simply pings a web service as a proof of concept
     * @throws IOException
     */
    // TODO delete this at some point
    public static void test() throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request r = new Request.Builder()
                .url("http://10.0.2.2:8080")
                .build();
        client.newCall(r).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                System.out.println("Response received");


            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.out.println("Oops");
                call.cancel();
            }
        });
    }
}
