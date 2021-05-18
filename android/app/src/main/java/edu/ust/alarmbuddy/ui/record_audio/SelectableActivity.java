package edu.ust.alarmbuddy.ui.record_audio;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import edu.ust.alarmbuddy.MainActivity;
import edu.ust.alarmbuddy.R;
import edu.ust.alarmbuddy.common.AlarmBuddyHttp;
import edu.ust.alarmbuddy.common.UserData;
import edu.ust.alarmbuddy.ui.friends.Profile;
import nl.bravobit.ffmpeg.ExecuteBinaryResponseHandler;
import nl.bravobit.ffmpeg.FFmpeg;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SelectableActivity extends AppCompatActivity implements SelectableViewHolder.OnItemSelectedListener {

    RecyclerView recyclerView;
    SelectableProfileAdapter adapter;
    private Button sendButton;
    private File formattedAudioFile;
    private OkHttpClient client;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_friends);
        client = new OkHttpClient();

        // set up listener for send button
        sendButton = findViewById(R.id.send_sound);
        sendButton.setOnClickListener(v -> {
            try {
                if (sendSound()) {
                    Toast.makeText(this, "Sound sent successfully", 3);
                } else {
                    Toast.makeText(this, "Sound could not be sent, try again", 3);
                }
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        });


        List<String> friends = null;
        try {
            friends = getFriends();
        }
        catch (InterruptedException e) {
            Log.e("getFriends", e.toString());
            friends = new ArrayList<>();
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Send Alarm to Friends");
        actionBar.setDisplayHomeAsUpEnabled(true);

        adapter = new SelectableProfileAdapter(this, getProfiles(friends));
        recyclerView = (RecyclerView)findViewById(R.id.selection_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }

    private List<String> getFriends() throws InterruptedException {

        ArrayList<String> friendNames = new ArrayList<>();

        String username = "";
        String token = "";
        username = UserData.getString(this, "username");
        token = UserData.getString(this, "token");

        Request request = new Request.Builder()
                .url(AlarmBuddyHttp.API_URL + "/FriendsWith/" + username)
                .header("Authorization", token)
                .build();


        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.i("Failure", e.toString());
                countDownLatch.countDown();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                final String myResponse;
                if (response.isSuccessful()) {
                    myResponse = response.body().string();
                    response.close();
                    Pattern pattern = Pattern.compile(":\"[A-Za-z0-9_]*?\"");
                    Matcher matcher = pattern.matcher(myResponse);

                    // for each friend, get their profile picture
                    while (matcher.find()) {
                        friendNames.add(matcher.group().substring(2, matcher.group().length()-1));
                    }

                }
                countDownLatch.countDown();
            }
        });

        countDownLatch.await();
        return friendNames;
    }

    // creates a list of SelectableProfiles from a list of usernames
    private List<SelectableProfile> getProfiles(List<String> names) {

        ArrayList<SelectableProfile> profileList = new ArrayList<>();

        for (int i = 0; i < names.size(); i++) {
            profileList.add(new SelectableProfile(names.get(i)));
        }

        return profileList;
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

    @Override
    public void onItemSelected(SelectableProfile selectableProfile) { }

    // makes database calls to send sound, returns whether it was successful
    private boolean sendSound() throws InterruptedException {
        boolean result = true;
        List<Profile> selectedProfiles = adapter.getSelectedItems();
        List<String> selectedUsernames = new ArrayList<>();

        // fill selectedUsernames with names of all selected friends
        for (Profile p : selectedProfiles) {
            selectedUsernames.add(p.getText1());
        }

        EditText soundNameText = findViewById(R.id.sound_name);
        String soundName = soundNameText.getText().toString();
        // upload sound
        uploadSound(soundName);
        Log.i("done uploading", "now sharing");
        String soundID = getSoundID(soundName);

        Log.i("SoundID", soundID);
        // share sound
        Log.i("friends", selectedUsernames.get(0));

        for (String friend : selectedUsernames) {
            Log.i("friends", friend);
            if (!shareSound(friend, soundID)) {
                result = false;
            }
        }

        Log.i("Done sharing", "now deleting");
        // delete sound
        if (!deleteSound(soundID)) {
            result = false;
        }

        return result;
    }


    public void uploadSound(String soundName) {
        if (FFmpeg.getInstance(this).isSupported()) {

            FFmpeg ffmpeg = FFmpeg.getInstance(this);

            String rawAudioPath = getExternalFilesDir("") + "/raw_recording.3gpp";
            formattedAudioFile = new File(getExternalFilesDir(""),  soundName + ".mp3");

            try {
                String[] cmd = new String[]{"-y", "-i", rawAudioPath,
                        formattedAudioFile.getAbsolutePath()};
                ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {
                    @Override
                    public void onFailure(String message) {
                        Log.i("Convert to mp3", "Failure " + message);
                    }

                    @Override
                    public void onSuccess(String message) {

                        String username = UserData.getString(SelectableActivity.this, "username");
                        String token = UserData.getString(SelectableActivity.this, "token");

                        RequestBody fileContent = RequestBody
                                .create(new File(formattedAudioFile.getAbsolutePath()),
                                        MediaType.parse("audio/mpeg"));

                        RequestBody body = new MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("file", formattedAudioFile.getName(), fileContent)
                                .addFormDataPart("soundDescription", "alarm sound")
                                .build();

                        Request request = new Request.Builder()
                                .url(AlarmBuddyHttp.API_URL + "/upload/" + username)
                                .header("Authorization", token)
                                .post(body)
                                .build();

                        CountDownLatch countDownLatch = new CountDownLatch(1);
                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                Log.i("Failure", e.toString());
                                countDownLatch.countDown();
                            }

                            @Override
                            public void onResponse(@NotNull Call call, @NotNull Response response)
                                    throws IOException {
                                Log.i("Response",
                                        response.toString() + " / " + response.body().string());
                                response.close();
                                countDownLatch.countDown();
                            }
                        });

                        try {
                            countDownLatch.await();
                        } catch (InterruptedException e) {
                            Log.i("Upload", e.toString());
                        }
                    }
                });
            } catch (Exception e) {
                Log.e("Cannot convert from 3gpp to mp3", "Exception: " + e);
            }
        }
    }

    /**
     * Shares the given sound file with the given friend.
     *
     * @param friendName User to share the sound with. Must be friends with given user.
     * @param soundID Sound ID of the sound to be shared.
     */
    public boolean shareSound(String friendName, String soundID) {
        final boolean[] result = {true};
        String username = UserData.getString(SelectableActivity.this, "username");
        String token = UserData.getString(SelectableActivity.this, "token");

        String URL = String.format("%s/shareSound/%s/%s/%s", AlarmBuddyHttp.API_URL, username, friendName, soundID);

        Request request = new Request.Builder()
                .url(URL)
                .header("Authorization", token)
                .post(RequestBody.create(null, ""))
                .build();

        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.i("Failure", e.toString());
                result[0] = false;
                countDownLatch.countDown();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response)
                    throws IOException {
                if (response.isSuccessful()) {
                    Log.i("Response",
                            response.toString() + " / " + response.body().string());
                }
                else {
                    result[0] = false;
                }
                countDownLatch.countDown();
            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result[0];
    }

    /**
     * Retrieves the soundID of the provided sound.
     * If there are multiple sounds with the same name, returns the last sound (highest soundID).
     *
     * @param soundName Name of sound to be retrieved.
     *
     * @return The soundID of the sound, if found, otherwise returns null
     */
    public String getSoundID(String soundName) {
        final String[] soundID = {null};

        String username = UserData.getString(this, "username");
        String token = UserData.getString(this, "token");

        String URL = String.format("%s/sounds/%s", AlarmBuddyHttp.API_URL, username);

        Request request = new Request.Builder()
                .url(URL)
                .header("Authorization", token)
                .get()
                .build();

        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.i("Failure", e.toString());
                countDownLatch.countDown();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response)
                    throws IOException {
                if (response.isSuccessful()) {
                    String myResponse = response.body().string();
                    response.close();

                    Pattern idPattern = Pattern.compile("\"soundID\":[0-9]*");
                    Matcher idMatcher = idPattern.matcher(myResponse);
                    Pattern namePattern = Pattern.compile("\"soundName\":\"[^\":]*");
                    Matcher nameMatcher = namePattern.matcher(myResponse);

                    // check each sound to see if it matches sound name
                    // if name matches, return sound ID
                    while (idMatcher.find() && nameMatcher.find()) {
                        if (nameMatcher.group().substring(13).equals(soundName)) {
                            soundID[0] = idMatcher.group().substring(10);
                            Log.i("Sound", soundID[0]);
                        }
                    }
                }
                else {
                    Log.i("Failure", response.toString());

                }
                countDownLatch.countDown();
            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return soundID[0];
    }

    public boolean deleteSound(String soundID) {
        boolean[] success = {true};
        String username = UserData.getString(this, "username");
        String token = UserData.getString(this, "token");

        String URL = String.format("%s/deleteSound/%s/%s", AlarmBuddyHttp.API_URL, username, soundID);
        Request request = new Request.Builder()
                .url(URL)
                .header("Authorization", token)
                .delete()
                .build();

        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.i("Delete Failure", e.toString());
                success[0] = false;
                countDownLatch.countDown();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                if (response.isSuccessful()) {
                    Log.i("Delete", response.toString());
                }
                else {
                    success[0] = false;
                }
                countDownLatch.countDown();
            }
        });
        try {
            countDownLatch.await();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        return success[0];
    }
}
