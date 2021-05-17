package edu.ust.alarmbuddy.ui.record_audio;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import edu.ust.alarmbuddy.R;
import edu.ust.alarmbuddy.common.AlarmBuddyHttp;
import edu.ust.alarmbuddy.common.UserData;
import edu.ust.alarmbuddy.ui.friends.Profile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

public class SelectableActivity extends AppCompatActivity implements
	SelectableViewHolder.OnItemSelectedListener {

	RecyclerView recyclerView;
	SelectableProfileAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_friends);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle("Send Alarm to Friends");
		actionBar.setDisplayHomeAsUpEnabled(true);

		List<String> friends = null;
		try {
			friends = getFriends();
		} catch (InterruptedException e) {
			Log.e("getFriends", e.toString());
			friends = new ArrayList<>();
		}

		adapter = new SelectableProfileAdapter(this, getProfiles(friends));
		recyclerView = (RecyclerView) findViewById(R.id.selection_list);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		recyclerView.setAdapter(adapter);
	}

	private List<String> getFriends() throws InterruptedException {
		OkHttpClient client = new OkHttpClient();
		ArrayList<String> friendNames = new ArrayList<>();

		String username = UserData.getStringNotNull(this, "username");
		String token = UserData.getStringNotNull(this, "token");

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
			public void onResponse(@NotNull Call call, @NotNull Response response)
				throws IOException {

				final String myResponse;
				if (response.isSuccessful()) {
					myResponse = response.body().string();
					response.close();
					Pattern pattern = Pattern.compile(":\"[A-Za-z0-9_]*?\"");
					Matcher matcher = pattern.matcher(myResponse);

					// for each friend, get their profile picture
					while (matcher.find()) {
						friendNames.add(matcher.group().substring(2, matcher.group().length() - 1));
					}
				}
				countDownLatch.countDown();
			}
		});

		countDownLatch.await();
		return friendNames;
	}

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
	public void onItemSelected(SelectableProfile selectableProfile) {

		List<Profile> selectedItems = adapter.getSelectedItems();

		String names = "";
		for (Profile p : selectedItems) {
			names += p.getText1() + ", ";
		}
		if (names.length() > 2) {
			names.substring(0, names.length() - 2);
			Snackbar.make(recyclerView, "Send alarm to " + names, Snackbar.LENGTH_LONG).show();
		}
	}
}
