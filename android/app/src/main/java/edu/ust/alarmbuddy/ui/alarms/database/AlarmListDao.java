package edu.ust.alarmbuddy.ui.alarms.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface AlarmListDao {

	@Query("SELECT * FROM alarms ORDER BY created ASC")
	LiveData<List<Alarm>> getAlarms();

	@Insert
	void insert(Alarm alarm);

	@Query("DELETE FROM alarms")
	void deleteAll();

	@Update
	void update(Alarm alarm);
}
