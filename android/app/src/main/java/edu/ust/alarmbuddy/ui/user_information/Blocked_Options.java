package edu.ust.alarmbuddy.ui.user_information;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import edu.ust.alarmbuddy.R;
import edu.ust.alarmbuddy.common.AlarmBuddyHttp;
import edu.ust.alarmbuddy.common.ProfilePictures;
import edu.ust.alarmbuddy.common.UserData;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Handles the activity that a user is shown when they select a blocked user from their blocked users list.
 * Creates an "Unblock user" button that sends an unblock user request to the database.
 */
public class Blocked_Options extends AppCompatActivity {

    private Bitmap picture;
    private ImageView image;
    private int flag;
    private TextView name;
    private Button unblock;
    private String u;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked__options);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Blocked User Options");
        actionBar.setDisplayHomeAsUpEnabled(true);

        image = findViewById(R.id.BOptionsImage);
        name = findViewById(R.id.BOptionsText);
        unblock = findViewById(R.id.UnblockUser);

        Intent intent = getIntent();
        name.setText(intent.getStringExtra("name"));
        picture = ProfilePictures.getProfilePic(getApplicationContext(), name.getText().toString());
        image.setImageBitmap(picture);

        //used to disable buttons after a successful database request. Set to an empty string to prevent errors.
        u="unblockUser";


        unblock.setOnClickListener(v -> {
            try {
                Post(u);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private void Post(String command) throws InterruptedException {
        OkHttpClient client = new OkHttpClient();
        if(command.compareTo("")==0){
            return;
        }
        flag = 0;

        String token  = UserData.getStringNotNull(this, "token");
        String username  = UserData.getStringNotNull(this, "username");

        String action = command;


        String url =
                AlarmBuddyHttp.API_URL + "/" + action + "/" + username + "/" + name.getText().toString()
                        .trim();
        Log.i(edu.ust.alarmbuddy.ui.user_information.Blocked_Options.class.getName(), "URL: " + url);

        Request request = new Request.Builder()
                .post(RequestBody.create("", MediaType.parse("text/plain")))
                .url(url)
                .header("Authorization", token)
                .build();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

                countDownLatch.countDown();
                call.cancel();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response)
                    throws IOException {
                Log.i(edu.ust.alarmbuddy.ui.friends.Friend_Options.class.getName(), "Code: " + response.code());
                Log.i(edu.ust.alarmbuddy.ui.friends.Friend_Options.class.getName(), "Message: " + response.body().string());
                if (response.isSuccessful()) {
                    flag = 1;
                    u="";
                    countDownLatch.countDown();
                }
            }
        });
        countDownLatch.await();

        if (flag == 1) {
            showToast("User Unblocked");

        } else if (flag == 0 ) {
            showToast("ERROR: Friend Was Not Removed");
        }


        flag = 0;
    }

    private void showToast(String input) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater
                .inflate(R.layout.friend_request_toast, findViewById(R.id.toast_root));

        TextView text = layout.findViewById(R.id.toast_text);
        text.setText(input);

        Toast toast = new Toast(this);
        toast.setGravity(Gravity.CENTER, 0, 200);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    /**
     * handles the back arrow at the top of the screen that allows the user to go back to the blocked user list.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == android.R.id.home) {
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}