package edu.ust.alarmbuddy;

import android.content.Context;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.gson.JsonParser;
import edu.ust.alarmbuddy.common.ProfilePictures;
import edu.ust.alarmbuddy.common.UserData;
import edu.ust.alarmbuddy.ui.user_information.UserInformationViewModel;
import android.util.Log;
import android.widget.TextView;
import androidx.lifecycle.ViewModelProviders;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class UserInformationActivity extends AppCompatActivity {
    private UserInformationViewModel userInformationViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_information_activity);
        UserInformationViewModel viewModel = ViewModelProviders.of(this).get(UserInformationViewModel.class);
        try {
            ArrayList<String> result = requestUserInformation(this.getApplicationContext());
            final ImageView img= findViewById(R.id.image);
            img.setImageBitmap(ProfilePictures.getProfilePic(this.getApplicationContext(), result.get(0)));
            final TextView username = findViewById(R.id.username);
            username.setText("Username: " + result.get(0));
            final TextView firstName = findViewById(R.id.first_name);
            firstName.setText("First Name: " + result.get(2));
            final TextView lastName = findViewById(R.id.last_name);
            lastName.setText("Last Name: " + result.get(3));
            final TextView email = findViewById(R.id.email);
            email.setText("Email: " + result.get(4));
            final TextView phoneNumber = findViewById(R.id.phone_number);
            phoneNumber.setText("Phone Number: " + result.get(5));
            final TextView birthday = findViewById(R.id.birthday);
            birthday.setText("Birthday: " + result.get(6));
            final TextView createdDate = findViewById(R.id.created_date);
            createdDate.setText("Account Creation Date: " + result.get(7));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public ArrayList<String> requestUserInformation(Context context) throws InterruptedException {

        OkHttpClient client = new OkHttpClient();
        ArrayList<String> infoList = new ArrayList<>();

        String token = "";
        try {
            token = UserData.getString(context, "token");
        } catch (Exception e) {
            e.printStackTrace();
        }

        String username = "";
        try {
            username = UserData.getString(context, "username");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Request request = new Request.Builder()
                .url("https://alarmbuddy-312620.uc.r.appspot.com/users/" + username)
                .header("Authorization", token)
                .build();

        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                call.cancel();
                countDownLatch.countDown();
                Log.e("Get User Information", "Failure " + e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response)
                    throws IOException {
                final String myResponse;
                if (response.isSuccessful()) {
                    myResponse = response.body().string();
                    response.close();
                    ArrayList<String> result = new ArrayList<>();
                    Pattern pattern = Pattern.compile("\"(.*?)\"");
                    Matcher matcher = pattern.matcher(myResponse);

                    while (matcher.find()) {
                        result.add(matcher.group(1));
                    }
                    for (int i = 2; i <= result.size(); i += 2) {
                        infoList.add(result.get(i));
                    }

                } else {
                    infoList.add("ElseResponse");
                }
                countDownLatch.countDown();
            }
        });
        countDownLatch.await();

        return infoList;
    }
}