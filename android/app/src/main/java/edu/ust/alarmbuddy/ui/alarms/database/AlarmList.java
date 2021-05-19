package edu.ust.alarmbuddy.ui.alarms.database;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;

public class AlarmList {

	private AlarmListDao alarmListDao;
	private LiveData<List<Alarm>> currentAlarmList;

	public AlarmList(Application application) {
		AlarmListDatabase alarmListDatabase = AlarmListDatabase.getDatabase(application);
		alarmListDao = alarmListDatabase.alarmListDao();
		currentAlarmList = alarmListDao.getAlarms();
	}

	public void insert(Alarm alarm) {
		AlarmListDatabase.databaseWriteExecutor.execute(() -> alarmListDao.insert(alarm));
	}

	public void update(Alarm alarm) {
		AlarmListDatabase.databaseWriteExecutor.execute(() -> alarmListDao.update(alarm));
	}

	public LiveData<List<Alarm>> getCurrentAlarmList() {
		return currentAlarmList;
	}

}
