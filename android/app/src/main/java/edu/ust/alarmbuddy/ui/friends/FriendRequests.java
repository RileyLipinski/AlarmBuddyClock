package edu.ust.alarmbuddy.ui.friends;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import edu.ust.alarmbuddy.R;
import edu.ust.alarmbuddy.common.AlarmBuddyHttp;
import edu.ust.alarmbuddy.common.UserData;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

public class FriendRequests extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Profile> mProfileList;


    public FriendRequests() {
        mRecyclerView = null;
        mAdapter = null;
        mLayoutManager = new LinearLayoutManager(this);
        mProfileList = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Inbox");
        actionBar.setDisplayHomeAsUpEnabled(true);

        int flag;
        ArrayList<Profile> profileList = new ArrayList<>();

        mRecyclerView = findViewById(R.id.inboxRecyclerView);
        try{
            flag = populateArray();
        }catch (InterruptedException e) {
            flag=0;
            e.printStackTrace();
        }

        if (flag == 0){
            TextView text = findViewById(R.id.inboxText);
            text.setText("Your inbox is empty");
        }
        mAdapter = new ProfileAdapter(getMProfileList(),1);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    private int populateArray() throws InterruptedException {
        int flag = 1;
        ArrayList<Profile> profileList = new ArrayList<>();
        ArrayList<String> nameList = new ArrayList<>();

        getRequest(nameList, this);

        if (nameList.size()==0) {
            flag=0;
        }

        //sorts the list of names alphabetically before using them to create Profile objects
        nameList.sort(String::compareToIgnoreCase);

        //uses the sorted names to create Profile objects
        for (String s : nameList) {
            profileList.add(new Profile(R.drawable.ic_baseline_account_box, s, "details"));
        }

        setMProfileList(profileList);
        return flag;
    }

    private static void getRequest(ArrayList<String> nameList, Context context)
            throws InterruptedException {
        //generates a get request from the database for a users friends list
        OkHttpClient client = new OkHttpClient();

        String token = UserData.getStringNotNull(context, "token");
        String username = UserData.getStringNotNull(context, "username");

        Request request = new Request.Builder()
                .url(AlarmBuddyHttp.API_URL + "/requests/" + username)
                .header("Authorization", token)
                .build();

        //insures that the get request is completed before the code continues
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                call.cancel();
                countDownLatch.countDown();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response)
                    throws IOException {
                final String myResponse;
                if (response.isSuccessful()) {
                    myResponse = response.body().string();
                    response.close();
                    Pattern pattern = Pattern.compile("\"([^\"]*)\"");
                    Matcher matcher = pattern.matcher(myResponse);
                    ArrayList<String> result = new ArrayList<>();

                    while (matcher.find()) {
                        result.add(matcher.group(1));
                    }
                    for (int i = 1; i < result.size(); i += 2) {
                        nameList.add(result.get(i));
                    }
                }
                countDownLatch.countDown();
            }
        });
        countDownLatch.await();
    }

    private ArrayList<Profile> getMProfileList() {
        return this.mProfileList;
    }

    private void setMProfileList(ArrayList<Profile> input) {
        mProfileList = input;
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