package edu.ust.alarmbuddy.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import edu.ust.alarmbuddy.R;

import java.time.LocalTime;

public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                ViewModelProviders.of(this).get(DashboardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        final TextView textView = root.findViewById(R.id.text_dashboard);
        dashboardViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        final EditText time = root.findViewById(R.id.editTextTime);
        time.setText("Enter alarm time here");

        final Button button = root.findViewById(R.id.createAlarm);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String entry = time.getText().toString();
                textView.setText(String.format("User typed out %s",entry));

                try {
                    LocalTime alarmTime = parseAlarmTime(entry);
                    System.out.println("hello world " + alarmTime.equals(LocalTime.parse(entry)));
                } catch (Exception e) {
                    System.out.println("Exception caught in alarm parser");
                    System.out.println("Exception is: " + e.getClass());
                    System.out.println("Message: " + e.getMessage());
                    System.out.println(e.getStackTrace());
                    textView.setText("Exception caught, check stack trace");
                }
            }

            private LocalTime parseAlarmTime(String in) {
                String[] x;

                if (in.contains(":")) {
                    if (in.length() != 5) {
                        throw new IllegalArgumentException(String.format("Cannot parse a valid time from %s",in));
                    }
                    x = in.split(":");
                } else {
                    if (in.length() != 4) {
                        throw new IllegalArgumentException(String.format("Cannot parse a valid time from %s",in));
                    }
                    x = new String[2];
                    x[0] = in.substring(0,2);
                    x[1] = in.substring(2);
                }
                if (x.length != 2) {
                    throw new IllegalArgumentException(String.format("Cannot parse a valid time from %s",in));
                }

                try {
                    int hours = Integer.parseInt(x[0]);
                    int mins = Integer.parseInt(x[1]);
                    return LocalTime.of(hours,mins);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(String.format("Cannot parse a valid time from %s",in));
                }
            }
        });
        return root;
    }
}