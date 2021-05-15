package edu.ust.alarmbuddy.ui.home;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import edu.ust.alarmbuddy.LoginActivity;
import edu.ust.alarmbuddy.R;
import edu.ust.alarmbuddy.common.UserData;
import okhttp3.*;
import okio.BufferedSink;
import okio.Okio;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class HomeFragment extends Fragment {

	private HomeViewModel homeViewModel;
	private View root;

	public View onCreateView(@NonNull LayoutInflater inflater,
		ViewGroup container, Bundle savedInstanceState) {
		homeViewModel =
			ViewModelProviders.of(this).get(HomeViewModel.class);
		root = inflater.inflate(R.layout.fragment_home, container, false);
		/*
		final TextView textView = root.findViewById(R.id.text_home);
		homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
			@Override
			public void onChanged(@Nullable String s) {
				textView.setText(s);
			}
		});
		*/

		return root;
	}

	// TODO: everything in here is just for the db testing session, delete later
	@Override
	public void onActivityCreated(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		OkHttpClient client = new OkHttpClient();

		Button getSoundsButton = root.findViewById(R.id.getSoundsButton);
		Button shareSoundButton = root.findViewById(R.id.shareSoundButton);
		Button deleteSoundButton = root.findViewById(R.id.deleteSoundButton);
		Button logoutButton = root.findViewById(R.id.logoutButton);
		EditText soundID = root.findViewById(R.id.soundID);
		EditText friendName = root.findViewById(R.id.friendName);
		EditText deleteSoundID = root.findViewById(R.id.deleteSoundID);

		String username = "";
		String token = "";


		username = UserData.getString(getContext(), "username");
		token = UserData.getString(getContext(), "token");
		Log.i("UserInfo", "Username: " + username + "\nToken: " + token);

		String finalUsername = username;
		String finalToken = token;
		getSoundsButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// get list of sounds, display on screen and print to log
				Request request = new Request.Builder()
						.url("https://alarmbuddy.wm.r.appspot.com/sounds/" + finalUsername)
						.header("Authorization", finalToken)
						.get()
						.build();

				client.newCall(request).enqueue(new Callback() {
					@Override
					public void onFailure(@NotNull Call call, @NotNull IOException e) {
						Log.i("Failure", e.toString());
					}

					@Override
					public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
						Log.i("Sound List", response.toString() + " / " +response.body().string());

					}
				});

			}
		});

		shareSoundButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// share sound with friend
				String friend = friendName.getText().toString();
				String sound = soundID.getText().toString();

				Request request = new Request.Builder()
						.url("https://alarmbuddy.wm.r.appspot.com/shareSound/" + finalUsername
							+ "/" + friend + "/" + sound)
						.header("Authorization", finalToken)
						.post(RequestBody.create(null, ""))
						.build();

				client.newCall(request).enqueue(new Callback() {
					@Override
					public void onFailure(@NotNull Call call, @NotNull IOException e) {
						Log.i("Failure", e.toString());
					}

					@Override
					public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
						Log.i("Share sound", response.toString() + " / " +response.body().string());
					}
				});

			}
		});

		deleteSoundButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				String sound = deleteSoundID.getText().toString();

				Request request = new Request.Builder()
						.url("https://alarmbuddy.wm.r.appspot.com/deleteSound/" + finalUsername
								+ "/" + sound)
						.header("Authorization", finalToken)
						.delete()
						.build();

				client.newCall(request).enqueue(new Callback() {
					@Override
					public void onFailure(@NotNull Call call, @NotNull IOException e) {
						Log.i("Failure", e.toString());
					}

					@Override
					public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
						Log.i("Delete sound", response.toString() + " / " +response.body().string());
					}
				});

			}
		});

		// logout -> destroy user info and return to login page
		logoutButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					UserData.clearSharedPreferences(getContext());
				}
				catch (GeneralSecurityException e) {
					Log.e("ClearSharedPreferences", e.toString());
				}
				catch (IOException e) {
					Log.e("ClearSharedPreferences", e.toString());
				}
				// move back to login screen
				getActivity().startActivity(new Intent(getActivity(), LoginActivity.class));
			}


		});

	}
}