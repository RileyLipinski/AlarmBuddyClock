package edu.ust.alarmbuddy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import edu.ust.alarmbuddy.ui.alarm.Alarm;
import edu.ust.alarmbuddy.ui.alarm.AlarmService;

import java.util.Calendar;
import java.util.Random;

public class AlarmActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        //dismiss button clicked
        final Button dismissButton = findViewById(R.id.activity_alarm_dismiss);
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AlarmService.class);
                getApplicationContext().stopService(intent);
                finish();
            }
        });

        //snooze button clicked (play in 10 minutes)
        final Button snoozeButton = findViewById(R.id.activity_alarm_snooze);
        snoozeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.add(Calendar.MINUTE, 10);

                Alarm alarm = new Alarm(
                        new Random().nextInt(Integer.MAX_VALUE),
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        "SNOOZE",
                        calendar.getTimeInMillis()
                );

                alarm.setAlarm(getApplicationContext());

                Intent intent = new Intent(getApplicationContext(), AlarmService.class);
                getApplicationContext().stopService(intent);
                finish();
            }
        });


    }

}