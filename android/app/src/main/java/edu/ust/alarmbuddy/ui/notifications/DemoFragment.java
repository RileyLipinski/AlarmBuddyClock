package edu.ust.alarmbuddy.ui.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import edu.ust.alarmbuddy.LoginActivity;
import edu.ust.alarmbuddy.R;
import edu.ust.alarmbuddy.common.UserData;
import edu.ust.alarmbuddy.ui.alarm.AlarmPublisher;
import edu.ust.alarmbuddy.worker.alarm.AlarmFetchReceiver;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class DemoFragment extends Fragment {

	private DemoViewModel demoViewModel;

	public View onCreateView(@NonNull LayoutInflater inflater,
		ViewGroup container, Bundle savedInstanceState) {
		demoViewModel =
			ViewModelProviders.of(this).get(DemoViewModel.class);
		View root = inflater.inflate(R.layout.fragment_demo, container, false);
		final TextView textView = root.findViewById(R.id.text_notifications);
		demoViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
			@Override
			public void onChanged(@Nullable String s) {
				textView.setText(s);
			}
		});
		final Button logoutButton = root.findViewById(R.id.logout_button);
		logoutButton.setOnClickListener(view -> {
			doLogout();
		});
		final Button demoButton = root.findViewById(R.id.demo_button);
		demoButton.setOnClickListener(view -> {
			demoButton();
		});
		return root;
	}

	public void demoButton() {
		Toast.makeText(getContext(), "Demo alarm sequence initiated", Toast.LENGTH_SHORT).show();

		Intent intent = new Intent(getContext(), AlarmFetchReceiver.class);
		intent.replaceExtras(new Bundle());
		intent.putExtra("wakeupTime", System.currentTimeMillis() + 10000L);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent, 0);
		AlarmPublisher.getAlarmManager(getContext())
			.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1, pendingIntent);
	}

	public void doLogout() {
		try {
			UserData.clearSharedPreferences(getContext());
			Intent logoutIntent = new Intent(getContext(), LoginActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString("event", "logout");
			logoutIntent.replaceExtras(bundle);
			startActivity(logoutIntent);
		} catch (GeneralSecurityException | IOException e) {
			Toast.makeText(getContext(), "LOGOUT FAILED", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}
}