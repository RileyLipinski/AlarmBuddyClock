package edu.ust.alarmbuddy.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import edu.ust.alarmbuddy.R;
import edu.ust.alarmbuddy.ui.alarm.AlarmNoisemaker;

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
		final Button demoButton = root.findViewById(R.id.demo_button);
		demoButton.setOnClickListener(view -> {
			demoButton();
		});
		return root;
	}

	public void demoButton() {
		startActivity(new Intent(getContext(),AlexaActivity.class));
	}
}