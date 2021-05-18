package edu.ust.alarmbuddy;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import com.google.gson.JsonParser;
import edu.ust.alarmbuddy.common.AlarmBuddyHttp;
import edu.ust.alarmbuddy.common.UserData;
import edu.ust.alarmbuddy.ui.login.FailedLoginDialogFragment;
import edu.ust.alarmbuddy.ui.login.LoginViewModel;
import edu.ust.alarmbuddy.worker.notification.NotificationFetchReceiver;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.concurrent.CountDownLatch;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;


public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    int loginAttempts = 0;
    TextView loginErrorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // if the user is logged in, redirect to the homepage
        if (userCurrentlyLoggedIn()) {
            loginToHome();
            return;
        }

        // for app link, is this necessary?
        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();

        LoginViewModel viewModel = ViewModelProviders.of(this).get(LoginViewModel.class);

        loginErrorText = findViewById(R.id.loginErrorText);
        final Button loginButton = findViewById(R.id.loginButton);
        final Button goToCreateAccountButton = findViewById(R.id.goToCreateAccountButton);

        loginButton.setOnClickListener(v -> {
            // get username/password from input
            TextView username1 = findViewById(R.id.textUsername);
            TextView password = findViewById(R.id.textPassword);

            // convert TextView to strings for comparison
            String stringUsername = username1.getText().toString();
            String stringPassword = password.getText().toString();

            try {
                if (authenticateLogin(stringUsername, stringPassword)
                        && loginAttempts < 4) {
                    loginToHome();
                } else {
                    loginAttempts++;
                }
            } catch (Exception e) {
                Log.d("TAG", e.toString());
            }
        });

        //final Button forgotPasswordButton = findViewById(R.id.loginButton)
        // TODO: forgot password action

        goToCreateAccountButton.setOnClickListener(v -> moveToCreateAccount());
    }

    /**
     * @return whether the user has valid login credentials
     */
    private boolean userCurrentlyLoggedIn() {
        Context context = getApplicationContext();

        String username = UserData.getString(context, "username");
        String token = UserData.getString(context, "token");

        if (username == null || token == null) {
            return false;
        }

        OkHttpClient client = new OkHttpClient();
        CountDownLatch latch = new CountDownLatch(1);
        final int[] code = new int[1];

        Request request = new Request.Builder()
                .get()
                .url(AlarmBuddyHttp.API_URL + "/users/" + username)
                .header("Authorization", token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                call.cancel();
                code[0] = 500;
                latch.countDown();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                code[0] = response.code();
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }

        return code[0] == 200;
    }

    private void loginToHome() {
        NotificationFetchReceiver.scheduleNotificationFetch(getApplicationContext());
        startActivity(new Intent(this, MainActivity.class));
    }

    private void moveToCreateAccount() {
        startActivity(new Intent(this, CreateAccountActivity.class));
    }

    private boolean authenticateLogin(String username, String password)
            throws IOException {
        //build the request
        String data = "username=" + username + "&password=" + password;
        URL url = new URL(AlarmBuddyHttp.API_URL + "/login");
        RequestBody body = RequestBody.create(data, MediaType
                .parse("application/x-www-form-urlencoded"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        //execute the request and wait for a response
        final String[] stringResponse = new String[1];
        final CountDownLatch latch = new CountDownLatch(1);
        AlarmBuddyHttp.client.newCall(request).enqueue(new Callback() {
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

        boolean trueResponse = stringResponse[0].substring(8, 12).equals("true");

        if (trueResponse) {
            String token = JsonParser.parseString(stringResponse[0])
                    .getAsJsonObject()
                    .get("token")
                    .getAsString();

            try {
                UserData.getSharedPreferences(getApplicationContext()).edit()
                        .putString("username", username)
                        .putString("token", token)
                        .apply();

            } catch (GeneralSecurityException e) {
                //TODO verify that this is acceptable behavior
                return false;
            }
        }


        else {
            loginErrorText.setText("Username or password is incorrect");
        }

        return stringResponse[0] != null && trueResponse;
    }

    @Override
    protected void onDestroy() {
        // TODO: does not clear info on force quitting app
        // when app is destroyed, also destroy user info
        // this only works when called from the same context that it was created in (LoginActivity)
        super.onDestroy();
        try {
            UserData.clearSharedPreferences(getApplicationContext());
        } catch (GeneralSecurityException e) {
            Log.e("ClearSharedPreferences", e.toString());
        } catch (IOException e) {
            Log.e("ClearSharedPreferences", e.toString());
        }
    }
}






