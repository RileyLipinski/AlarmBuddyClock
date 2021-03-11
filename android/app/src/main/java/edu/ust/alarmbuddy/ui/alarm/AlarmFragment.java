package edu.ust.alarmbuddy.ui.alarm;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import edu.ust.alarmbuddy.R;

public class AlarmFragment extends Fragment {

    private AlarmViewModel alarmViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        alarmViewModel = new ViewModelProvider(this).get(AlarmViewModel.class);
        View root = inflater.inflate(R.layout.fragment_alarm, container, false);

        final TextView textView = root.findViewById(R.id.text_alarm);
        alarmViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

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
        alarmMinutes.setAdapter(minutesAdapter);

        final Button button = root.findViewById(R.id.createAlarm);
        button.setOnClickListener(view -> {
            int hours = Integer.parseInt((String) alarmHours.getSelectedItem());
            int minutes = Integer.parseInt((String) alarmMinutes.getSelectedItem());

            AlarmPublisher
                    .publishAlarm(getContext(), (int) (System.currentTimeMillis() % 10000000L), hours,
                            minutes);
        });
        return root;
    }
}