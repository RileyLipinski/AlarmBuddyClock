package edu.ust.alarmbuddy.ui.alarms;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import edu.ust.alarmbuddy.R;
import edu.ust.alarmbuddy.ui.alarm.Alarm;

public class AlarmListFragment extends Fragment implements OnToggleAlarmListener {

	private AlarmListRecyclerViewAdapter alarmListRecyclerViewAdapter;
	private AlarmListViewModel alarmListViewModel;
	private RecyclerView recyclerView;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		alarmListRecyclerViewAdapter = new AlarmListRecyclerViewAdapter(this);
		//alarmListViewModel = ViewModelProviders.of(this).get(AlarmListViewModel.class);
		//alarmListViewModel.getCurrentAlarms().observe(this, new Observer<List<Alarm>>() {

	}

	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
		@Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_alarm_list, container, false);

		recyclerView = view.findViewById(R.id.fragment_alarm_list_recyclerView);
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		recyclerView.setAdapter(alarmListRecyclerViewAdapter);

		return view;
	}

	@Override
	public void onToggle(Alarm alarm) {

	}
}
