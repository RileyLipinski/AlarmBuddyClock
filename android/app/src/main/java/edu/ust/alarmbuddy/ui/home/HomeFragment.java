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
import edu.ust.alarmbuddy.common.AlarmBuddyHttp;
import edu.ust.alarmbuddy.common.UserData;
import edu.ust.alarmbuddy.ui.alarm.Alarm;
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

		return root;
	}


}