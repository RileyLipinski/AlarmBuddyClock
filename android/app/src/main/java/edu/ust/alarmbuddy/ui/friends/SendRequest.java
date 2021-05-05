package edu.ust.alarmbuddy.ui.friends;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import edu.ust.alarmbuddy.R;
import edu.ust.alarmbuddy.common.AlarmBuddyHttp;
import edu.ust.alarmbuddy.common.UserData;
import java.io.IOException;
import java.security.GeneralSecurityException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

/***
 * @author Keghan Halloran
 * This activity provides the framework for sending friend requests
 * it currently does nothing other than displaying a message to the user
 * since we do not have the ability to add to a users friends list
 * or send a user a friend request yet. Implementing the intended usage
 * is dependant on further collaboration with the database team.
 */
public class SendRequest extends AppCompatActivity {

	private Button button;
	private EditText entry;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_send_request);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle("Send A Friend Request");
		actionBar.setDisplayHomeAsUpEnabled(true);

		button = findViewById(R.id.button);
		entry = findViewById(R.id.textUsername);
		entry.setText(" ");
		button.setOnClickListener(v -> {
			try {
				send();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});


	}

	private void send() throws InterruptedException {
		OkHttpClient client = new OkHttpClient();

		String token = "";
		try {
			token = UserData.getString(this, "token");
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String username = "";
		try {
			username = UserData.getString(this, "username");
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String url =
			AlarmBuddyHttp.API_URL + "/sendRequest/" + username + "/" + entry.getText().toString()
				.trim();
		Log.i(SendRequest.class.getName(), "URL: " + url);

		Request request = new Request.Builder()
			.post(RequestBody.create("", MediaType.parse("text/plain")))
			.url(url)
			.header("Authorization", token)
			.build();

		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(@NotNull Call call, @NotNull IOException e) {
				showToast("Request could not be sent");
			}

			@Override
			public void onResponse(@NotNull Call call, @NotNull Response response)
				throws IOException {
				Log.i(SendRequest.class.getName(), "Code: " + response.code());
				Log.i(SendRequest.class.getName(), "Message: " + response.body().string());
				if (response.isSuccessful()) {
					showToast("Request sent successfully"); //TODO this toast is throwing an error:
					//2021-05-05 16:39:10.868 30946-30985/edu.ust.alarmbuddy E/AndroidRuntime: FATAL EXCEPTION: OkHttp Dispatcher
					//    Process: edu.ust.alarmbuddy, PID: 30946
					//    java.lang.RuntimeException: Can't toast on a thread that has not called Looper.prepare()
					//        at android.widget.Toast$TN.<init>(Toast.java:390)
					//        at android.widget.Toast.<init>(Toast.java:114)
					//        at android.widget.Toast.<init>(Toast.java:105)
					//        at edu.ust.alarmbuddy.ui.friends.SendRequest.showToast(SendRequest.java:124)
					//        at edu.ust.alarmbuddy.ui.friends.SendRequest.access$000(SendRequest.java:41)
					//        at edu.ust.alarmbuddy.ui.friends.SendRequest$1.onResponse(SendRequest.java:111)
					//        at okhttp3.internal.connection.RealCall$AsyncCall.run(RealCall.kt:519)
					//        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1162)
					//        at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:636)
					//        at java.lang.Thread.run(Thread.java:764)
				}
			}
		});
	}

	private void showToast(String input) {
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater
			.inflate(R.layout.friend_request_toast, findViewById(R.id.toast_root));

		TextView text = layout.findViewById(R.id.toast_text);
		text.setText(input);

		Toast toast = new Toast(this);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(layout);
		toast.show();
	}

	//allows the back arrow at the top of this activity to go back to the Friends Fragment instead of a parent activity
	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		super.onOptionsItemSelected(item);
		if (item.getItemId() == android.R.id.home) {
			setResult(Activity.RESULT_CANCELED);
			finish();
		}
		return super.onOptionsItemSelected(item);
	}
}