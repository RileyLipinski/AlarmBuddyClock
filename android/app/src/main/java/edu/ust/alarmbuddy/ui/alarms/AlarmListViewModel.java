package edu.ust.alarmbuddy.ui.alarms;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import edu.ust.alarmbuddy.ui.alarms.database.Alarm;
import edu.ust.alarmbuddy.ui.alarms.database.AlarmList;

import java.util.List;

public class AlarmListViewModel extends AndroidViewModel {

	private AlarmList alarmList;
	private LiveData<List<Alarm>> currentAlarms;

	public AlarmListViewModel(@NonNull Application application) {
		super(application);

		alarmList = new AlarmList(application);
		currentAlarms = alarmList.getCurrentAlarmList();

	}

	public void update(Alarm alarm) {
		alarmList.update(alarm);
	}

	public LiveData<List<Alarm>> getCurrentAlarms() {
		return currentAlarms;
	}
}