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
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import edu.ust.alarmbuddy.R;
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
	private Button button;

	public FriendsFragment() {
		mRecyclerView = null;
		mAdapter = null;
		mlayoutManager = new LinearLayoutManager(getContext());
		mProfileList = null;
		button = null;
	}

	private void populateArray() throws InterruptedException {
		ArrayList<Profile> profileList = new ArrayList<>();
		ArrayList<String> nameList = new ArrayList<>();

		getRequest(nameList, getActivity().getApplicationContext());

		for (int i = 0; i < 10; i++) {
			nameList.add("Placeholder");
		}

		//sorts the list of names alphabetically before using them to create Profile objects
		nameList.sort(String::compareToIgnoreCase);

		//uses the sorted names to create Profile objects
		for (String s : nameList) {
			profileList.add(new Profile(R.drawable.ic_baseline_account_box, s, "details"));
		}

		setMProfileList(profileList);
	}

	private static void getRequest(ArrayList<String> nameList, Context context)
		throws InterruptedException {
		//generates a get request from the database for a users friends list
		//currently using hardcoded values, as dynamically obtaining all relevant user data is not yet possible.
		OkHttpClient client = new OkHttpClient();

		String token = "";
		try {
			token = UserData.getString(context, "token");
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String username = "";
		try {
			username = UserData.getString(context, "username");
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Request request = new Request.Builder()
			.url("https://alarmbuddy.wm.r.appspot.com/FriendsWith/" + username)
			.header("Authorization", token)
			.build();

		//insures that the get request is completed before the code continues
		CountDownLatch countDownLatch = new CountDownLatch(1);
		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(@NotNull Call call, @NotNull IOException e) {
				nameList.add("Failure");
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

				} else {
					nameList.add("ElseResponse");
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
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_friends, container, false);

		buildButton(v);

		setHasOptionsMenu(true);
		try {
			buildRecyclerView(v);
		} catch (InterruptedException e) {
			e.printStackTrace();
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
		button = v.findViewById(R.id.button);
		button.setOnClickListener(v1 -> openSendRequest());
	}

	private void openSendRequest() {
		Intent intent = new Intent(getActivity(), SendRequest.class);
		startActivity(intent);
	}

	private void buildRecyclerView(View v) throws InterruptedException {
		mRecyclerView = v.findViewById(R.id.recyclerView);
		populateArray();
		mAdapter = new ProfileAdapter(getMProfileList());
		mRecyclerView.setLayoutManager(mlayoutManager);
		mRecyclerView.setAdapter(mAdapter);
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