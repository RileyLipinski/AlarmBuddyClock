package edu.ust.alarmbuddy.ui.dashboard;

import android.content.Context;
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
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.ust.alarmbuddy.MainActivity;
import edu.ust.alarmbuddy.R;
import edu.ust.alarmbuddy.common.AlarmBuddyUtils;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Date;

public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;
    private static final String CHANNEL_ID = "alarms";

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
            public void onClick(View view) {
                String entry = time.getText().toString();
                textView.setText(String.format("User typed out %s",entry));

                String formattedDate = AlarmBuddyUtils.DATE_FORMAT.format(new Date());
                textView.setText(formattedDate);
                System.out.println(formattedDate);
                OkHttpClient client = new OkHttpClient();
                Request r = new Request.Builder()
                        .url("http://10.0.2.2:8080/json-test")
                        .build();
                client.newCall(r).enqueue(new Callback() {
                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        System.out.println("Response received");
                        JsonObject object = JsonParser.parseString(response.body().string()).getAsJsonObject();
                        String value = object.get("key").getAsString();

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textView.setText(value);
                            }
                        });
                    }

                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        System.out.println("Oops");
                        call.cancel();
                    }
                });
                try {
                    createAlarmNotification();
                } catch (Exception ignored) {

                }
            }
        });
        return root;
    }

    private void createAlarmNotification() {
        //TODO not scheduled for the future, which needs to change
        //TODO currently not actually popping up on screen, but it does show up in the notification center
        System.out.println("setting alarm");

        Context context = this.getContext();

        assert context != null;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MainActivity.CHANNEL_ID);

        builder
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setContentTitle("Hello world")
            .setContentText("This is a notification")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true);

        NotificationManagerCompat.from(context).notify(123,builder.build());
    }


}