package edu.ust.alarmbuddy.ui.alarm;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.TimePicker;
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

        /*final TextView textView = root.findViewById(R.id.text_alarm);
        alarmViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);*/
/*
        // TODO change this to not require 24-hour time
        final Spinner alarmHours = root.findViewById(R.id.alarm_hours);
        ArrayAdapter<CharSequence> hoursAdapter = ArrayAdapter
                .createFromResource(getContext(), R.array.hours_array,
                        android.R.layout.simple_spinner_item);
        hoursAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        alarmHours.setAdapter(hoursAdapter);

        final Spinner alarmMinutes = root.findViewById(R.id.alarm_minutes);
        ArrayAdapter<CharSequence> minutesAdapter = ArrayAdapter
                .createFromResource(getContext(), R.array.minutes_array,
                        android.R.layout.simple_spinner_item);
        hoursAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        alarmMinutes.setAdapter(minutesAdapter);*/

        final Button button = root.findViewById(R.id.fragment_alarm_createAlarm);
        button.setOnClickListener(view -> {
            final TimePicker timePicker = root.findViewById(R.id.fragment_alarm_clock);
            int alarmID = new Random().nextInt(Integer.MAX_VALUE);
            Alarm alarm = new Alarm(alarmID, timePicker.getHour(), timePicker.getMinute(),  false, false, false, false, false, false, false, "Alarm", System.currentTimeMillis());
            alarmViewModel.insert(alarm);

            alarm.setAlarm(getContext());

        });
        return root;
    }
}