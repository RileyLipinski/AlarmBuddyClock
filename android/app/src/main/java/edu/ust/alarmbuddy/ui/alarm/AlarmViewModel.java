package edu.ust.alarmbuddy.ui.alarm;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import edu.ust.alarmbuddy.ui.alarms.database.Alarm;
import edu.ust.alarmbuddy.ui.alarms.database.AlarmList;
import org.jetbrains.annotations.NotNull;

public class AlarmViewModel extends AndroidViewModel {
	private AlarmList alarmList;

	public AlarmViewModel(@NonNull Application application) {
		super(application);
		alarmList = new AlarmList(application);
	}

	public void insert(Alarm alarm) {
		alarmList.insert(alarm);
	}
}