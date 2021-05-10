package edu.ust.alarmbuddy.ui.friends;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import edu.ust.alarmbuddy.R;
import edu.ust.alarmbuddy.common.AlarmBuddyHttp;
import edu.ust.alarmbuddy.common.UserData;
import java.io.IOException;
import java.security.GeneralSecurityException;
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



/***
 * @author Keghan Halloran
 * This is the Fragment responsible for a users friends list. it does the following:
 * Obtains the users friends list from the database using an OKHTTP request.
 * Generates profile objects for those friends and displays them in a recyclerview.
 * Creates a "Send Friend Request" button that redirects a user to the SendRequest activity
 * that handles sending friend requests to other users.
 */
public class FriendsFragment extends Fragment {

	private RecyclerView mRecyclerView;
	private ProfileAdapter mAdapter;
	private final RecyclerView.LayoutManager mlayoutManager;
	private ArrayList<Profile> mProfileList;
	private Button sendButton;
	private Button inButton;

	public FriendsFragment() {
		mRecyclerView = null;
		mAdapter = null;
		mlayoutManager = new LinearLayoutManager(getContext());
		mProfileList = null;
		sendButton = null;
		inButton = null;
	}

	private int populateArray() throws InterruptedException {
		int flag = 1;
		ArrayList<Profile> profileList = new ArrayList<>();
		ArrayList<String> nameList = new ArrayList<>();

		getRequest(nameList, getActivity().getApplicationContext());

		if (nameList.size() == 0) {
			flag = 0;
		}

		//sorts the list of names alphabetically before using them to create Profile objects
		nameList.sort(String::compareToIgnoreCase);

		//uses the sorted names to create Profile objects
		int i =0;
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
			.url(AlarmBuddyHttp.API_URL + "/FriendsWith/" + username)
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
		Bundle savedInstanceState) {
		int flag;
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_friends, container, false);

		buildButton(v);

		setHasOptionsMenu(true);
		try {
			flag = buildRecyclerView(v);
		} catch (InterruptedException e) {
			flag = 0;
			e.printStackTrace();
		}
		if (flag == 0) {
			TextView text = v.findViewById(R.id.text_friends);
			text.setText(
				"You don't currently have any AlarmBuddy friends. Press the send friend request button to begin adding friends.");
		}
		return v;
	}

	private ArrayList<Profile> getMProfileList() {
		return this.mProfileList;
	}

	private void setMProfileList(ArrayList<Profile> input) {
		mProfileList = input;
	}

	private void buildButton(View v) {
		sendButton = v.findViewById(R.id.send_button);
		sendButton.setOnClickListener(v1 -> openSendRequest());

		inButton = v.findViewById(R.id.in_button);
		inButton.setOnClickListener(v1 -> openInbox());

	}

	private void openSendRequest() {
		Intent intent = new Intent(getActivity(), SendRequest.class);
		startActivity(intent);
	}

	private void openInbox() {
		Intent intent = new Intent(getActivity(), FriendRequests.class);
		startActivity(intent);
	}


	private int buildRecyclerView(View v) throws InterruptedException {
		int flag;
		mRecyclerView = v.findViewById(R.id.recyclerView);
		flag = populateArray();
		mAdapter = new ProfileAdapter(getMProfileList(),0);
		mRecyclerView.setLayoutManager(mlayoutManager);
		mRecyclerView.setAdapter(mAdapter);
		return flag;
	}

	@Override
	public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.search_menu, menu);

		androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) menu
			.findItem(R.id.action_search).getActionView();

		searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				mAdapter.getFilter().filter(newText);
				return false;
			}
		});
	}

}