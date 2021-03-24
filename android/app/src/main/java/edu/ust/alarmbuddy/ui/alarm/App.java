package edu.ust.alarmbuddy.ui.alarm;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import androidx.annotation.RequiresApi;

public class App extends Application {

    //API 26
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    //API 26
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createNotificationChannel() {
        NotificationChannel  notificationChannel = new NotificationChannel(NotificationChannel.DEFAULT_CHANNEL_ID, "Alarm Service Channel", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager notificationManager = getSystemService((NotificationManager.class));
        notificationManager.createNotificationChannel(notificationChannel);
    }
}
