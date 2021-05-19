package edu.ust.alarmbuddy.ui.alarms;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import edu.ust.alarmbuddy.R;
import edu.ust.alarmbuddy.ui.alarms.database.Alarm;
import java.util.ArrayList;
import java.util.List;

public class AlarmListRecyclerViewAdapter extends RecyclerView.Adapter<AlarmListViewHolder> {

	private List<Alarm> alarmList;
	private OnToggleAlarmListener listener;

	public AlarmListRecyclerViewAdapter(OnToggleAlarmListener listener) {
		this.alarmList = new ArrayList<Alarm>();
		this.listener = listener;
	}

	@NonNull
	@Override
	public AlarmListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.alarm_list,
			parent, false);
		return new AlarmListViewHolder(itemView, listener);
	}

	@Override
	public void onBindViewHolder(@NonNull AlarmListViewHolder holder, int position) {
		Alarm alarm = alarmList.get(position);
		holder.bind(alarm);
	}

	/**
	 * Returns the total number of items in the data set held by the adapter.
	 *
	 * @return The total number of items in this adapter.
	 */
	@Override
	public int getItemCount() {
		return alarmList.size();
	}

	@Override
	public void onViewRecycled(@NonNull AlarmListViewHolder holder) {
		super.onViewRecycled(holder);
		holder.alarmStarted.setOnCheckedChangeListener(null);
	}

	public void setAlarms(List<Alarm> alarmList) {
		this.alarmList = alarmList;
		notifyDataSetChanged();
	}
}
