package edu.ust.alarmbuddy.ui.alarm;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import edu.ust.alarmbuddy.R;
import java.util.Random;

public class AlarmFragment extends Fragment {

	private AlarmViewModel alarmViewModel;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		alarmViewModel = ViewModelProviders.of(this).get(AlarmViewModel.class);
	}


	public View onCreateView(@NonNull LayoutInflater inflater,
		ViewGroup container, Bundle savedInstanceState) {

		AlarmViewModel alarmViewModel = new ViewModelProvider(this).get(AlarmViewModel.class);
		View root = inflater.inflate(R.layout.fragment_alarm, container, false);

		final Button button = root.findViewById(R.id.fragment_alarm_createAlarm);
		button.setOnClickListener(view -> {
			final TimePicker timePicker = root.findViewById(R.id.fragment_alarm_clock);
			int alarmID = new Random().nextInt(Integer.MAX_VALUE);
			Alarm alarm = new Alarm(alarmID, timePicker.getHour(), timePicker.getMinute(), false,
				false, false, false, false, false, false, "Alarm", System.currentTimeMillis());
//            alarmViewModel.insert(alarm);

			alarm.setAlarm(getContext());

			AlarmPublisher.publishAlarm(getContext(), timePicker.getHour(), timePicker.getMinute());
			Toast.makeText(getContext(), String
					.format("Setting time for %2d:%2d", timePicker.getHour(), timePicker.getMinute()),
				Toast.LENGTH_SHORT);
		});
		return root;
	}
}