package edu.ust.alarmbuddy.ui.alarms;

import android.os.Build;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import edu.ust.alarmbuddy.ui.alarm.Alarm;
import org.jetbrains.annotations.NotNull;

import edu.ust.alarmbuddy.R;

public class AlarmListViewHolder extends RecyclerView.ViewHolder {
    private TextView alarmTime;
    private TextView alarmDays;
    private TextView alarmName;
    private OnToggleAlarmListener listener;

    public AlarmListViewHolder(@NonNull View itemView) {
        super(itemView);

        alarmTime = itemView.findViewById(R.id.alarm_list_alarm);
        this.listener = listener;
    }

    public void bind(Alarm alarm, OnToggleAlarmListener listener) {
        String alarmString = String.format("%02d:%02d", alarm.getHour(), alarm.getMinute());

        alarmTime.setText(alarmString);
    }
}