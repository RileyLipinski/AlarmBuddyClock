package edu.ust.alarmbuddy.ui.alarms;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import edu.ust.alarmbuddy.R;

public class AlarmsFragment extends Fragment {

    private AlarmsViewModel alarmsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        alarmsViewModel = ViewModelProviders.of(this).get(AlarmsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_alarms, container, false);
        final TextView textView = root.findViewById(R.id.alarm_text);
        final Switch alarmSwitch = root.findViewById(R.id.alarmSwitch);
        alarmsViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                if (alarmSwitch.isChecked()) {
                    textView.setText(s);
                }
            }
        });
        return root;
    }
}
