package edu.ust.alarmbuddy.ui.friends;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
import edu.ust.alarmbuddy.R;
import edu.ust.alarmbuddy.common.AlarmBuddyHttp;
import edu.ust.alarmbuddy.common.UserData;
import java.io.IOException;
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

public class Request_Options extends AppCompatActivity {
    private ImageView picture;
    private int flag;
    private TextView name;
    private Button accept;
    private Button deny;
    private Button block;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_options);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Request Options");
        actionBar.setDisplayHomeAsUpEnabled(true);

        picture = findViewById(R.id.ROptionsImage);
        name = findViewById(R.id.ROptionsText);
        accept = findViewById(R.id.Accept);
        deny = findViewById(R.id.Deny);
        block = findViewById(R.id.Block);

        Intent intent = getIntent();
        name.setText(intent.getStringExtra("name"));

        accept.setOnClickListener(v -> {
            try {
                Post("accept");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        deny.setOnClickListener(v -> {
            try {
                Post("deny");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        block.setOnClickListener(v -> {
            try {
                Post("block");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private void Post(String command) throws InterruptedException {
        OkHttpClient client = new OkHttpClient();
        flag = 0;

        String token = UserData.getStringNotNull(this, "token");
        String username = UserData.getStringNotNull(this, "username");

        String action = "";
        if (command.compareTo("accept")==0){
            //Special attention
            action = "acceptFriendRequest";
            String url =
                    AlarmBuddyHttp.API_URL + "/" + action + "/" + name.getText().toString().trim()
                            +"/" + username;
        }
        else if (command.compareTo("deny")==0){
            action = "denyFriendRequest";String url =
                    AlarmBuddyHttp.API_URL + "/" + action + "/" + name.getText().toString().trim()
                            +"/" + username;

        }
        else if(command.compareTo("block")==0){
            action = "";
        }

        String url =
                AlarmBuddyHttp.API_URL + "/" + action + "/" + username + "/" + name.getText().toString()
                        .trim();
        Log.i(Friend_Options.class.getName(), "URL: " + url);

        Request request = new Request.Builder()
                .post(RequestBody.create("", MediaType.parse("text/plain")))
                .url(url)
                .header("Authorization", token)
                .build();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {countDownLatch.countDown();}

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response)
                    throws IOException {
                Log.i(Friend_Options.class.getName(), "Code: " + response.code());
                Log.i(Friend_Options.class.getName(), "Message: " + response.body().string());
                if (response.isSuccessful()) {
                    flag=1;
                    countDownLatch.countDown();
                }
            }
        });
        countDownLatch.await();

        if(flag == 1 && command.compareTo("accept")==0){
            showToast("Request Accepted");

        }else if (flag ==0 && command.compareTo("accept")==0) {
            showToast("ERROR: Request could not be accepted");

        } else if (flag==1 && command.compareTo("deny")==0){
            showToast("Request Denied");

        }else if (flag ==0 && command.compareTo("deny")==0){
            showToast("ERROR: Request could not be denied");

        }else if (flag ==1 && command.compareTo("block")==0){
            showToast("User Blocked");

        }else if (flag ==0 && command.compareTo("block")==0){
            showToast("ERROR: User Was Not Blocked");
        }

        flag =0;
    }

    private void showToast(String input) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater
                .inflate(R.layout.friend_request_toast, findViewById(R.id.toast_root));

        TextView text = layout.findViewById(R.id.toast_text);
        text.setText(input);

        Toast toast = new Toast(this);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

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