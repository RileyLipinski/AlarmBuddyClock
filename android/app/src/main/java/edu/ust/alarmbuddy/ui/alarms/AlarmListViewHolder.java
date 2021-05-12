package edu.ust.alarmbuddy.ui.alarms;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import edu.ust.alarmbuddy.ui.alarms.database.Alarm;

import edu.ust.alarmbuddy.R;

public class AlarmListViewHolder extends RecyclerView.ViewHolder {
    Switch alarmStarted;
    private TextView alarmTime;
    private TextView alarmDays;
    private TextView alarmName;
    private OnToggleAlarmListener listener;

    public AlarmListViewHolder(@NonNull View itemView, OnToggleAlarmListener listener) {
        super(itemView);

        alarmStarted = itemView.findViewById(R.id.alarm_list_alarm_started);
        alarmTime = itemView.findViewById(R.id.alarm_list_alarm);

    }

    public void bind(Alarm alarm) {
        String alarmString = String.format("%02d:%02d", alarm.getHour(), alarm.getMinute());

        alarmTime.setText(alarmString);

        alarmStarted.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                listener.onToggle(alarm);
            }
        });
    }
}
