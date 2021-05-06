package edu.ust.alarmbuddy.ui.alarms.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Alarm.class}, version = 1, exportSchema = false)
public abstract class AlarmListDatabase extends RoomDatabase {
    public abstract AlarmListDao alarmListDao();
    private static volatile AlarmListDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static AlarmListDatabase getDatabase(final Context context) {
        if(INSTANCE == null) {
            synchronized (AlarmListDatabase.class) {
                if(INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AlarmListDatabase.class, "alarms").build();
                }
            }
        }
        return INSTANCE;
    }

}
