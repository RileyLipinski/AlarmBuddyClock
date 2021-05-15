package edu.ust.alarmbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import edu.ust.alarmbuddy.ui.alarm.Alarm;
import edu.ust.alarmbuddy.worker.alarm.AlarmService;
import java.util.Calendar;
import java.util.Random;

public class AlarmActivity extends AppCompatActivity {

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alarm);

		//dismiss button clicked
		final Button dismissButton = findViewById(R.id.activity_alarm_dismiss);
		dismissButton.setOnClickListener(v -> {
			//stop alarm service
			Intent intent = new Intent(getApplicationContext(), AlarmService.class);
			getApplicationContext().stopService(intent);
			finish();
		});

		//snooze button clicked (play in 10 minutes)
		final Button snoozeButton = findViewById(R.id.activity_alarm_snooze);
		snoozeButton.setOnClickListener(v -> {
			//get current time
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(System.currentTimeMillis());
			calendar.add(Calendar.MINUTE, 10);
			//create a new alarm for 10 minutes in future
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
				calendar.getTimeInMillis(),
				true
			);
			//set the new alarm that was created
			alarm.setAlarm(getApplicationContext());
			//stop previous alarm service
			Intent intent = new Intent(getApplicationContext(), AlarmService.class);
			getApplicationContext().stopService(intent);
			finish();
		});


	}

}