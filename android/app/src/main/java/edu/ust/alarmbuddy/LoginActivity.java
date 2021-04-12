package edu.ust.alarmbuddy;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import edu.ust.alarmbuddy.common.AlarmBuddyHttp;
import edu.ust.alarmbuddy.ui.login.FailedLoginDialogFragment;
import edu.ust.alarmbuddy.ui.login.LoginViewModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class LoginActivity extends AppCompatActivity {

	private LoginViewModel loginViewModel;
	int loginAttempts = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		LoginViewModel viewModel = ViewModelProviders.of(this).get(LoginViewModel.class);

		final Button loginButton = findViewById(R.id.loginButton);
		final Button goToCreateAccountButton = findViewById(R.id.goToCreateAccountButton);

		loginButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				// get username/password from input
				TextView username = findViewById(R.id.textUsername);
				TextView password = findViewById(R.id.textPassword);

				// convert TextView to strings for comparison
				String stringUsername = username.getText().toString();
				String stringPassword = password.getText().toString();

				// if username and password match, "login" to homepage
				AlarmBuddyHttp h = new AlarmBuddyHttp();
				try {
					if (authenticateLogin(stringUsername, stringPassword, getApplicationContext())
						&& loginAttempts < 4) {
						loginToHome();
					} else {
						loginAttempts++;
						FailedLoginDialogFragment dialog = new FailedLoginDialogFragment();
						dialog.show(getSupportFragmentManager(), "TAG");
					}
				} catch (Exception e) {
					Log.d("TAG", e.toString());
				}
			}
		});

		//final Button forgotPasswordButton = findViewById(R.id.loginButton)
		// TODO: forgot password action

		goToCreateAccountButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				moveToCreateAccount();
			}
		});

	}

	public void loginToHome() {
		startActivity(new Intent(this, MainActivity.class));

	}

	private void moveToCreateAccount() {
		startActivity(new Intent(this, CreateAccountActivity.class));
	}

	public static boolean authenticateLogin(String username, String password, Context context)
		throws IOException {

		//build the request
		String data = "username=" + username + "&password=" + password;
		URL url = new URL("https://alarmbuddy.wm.r.appspot.com/login");
		RequestBody body = RequestBody.create(data, MediaType
			.parse("application/x-www-form-urlencoded"));
		Request request = new Request.Builder()
			.url(url)
			.post(body)
			.build();

		//execute the request and wait for a response
		final String[] stringResponse = new String[1];
		final CountDownLatch latch = new CountDownLatch(1);
		AlarmBuddyHttp.client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				call.cancel();
				latch.countDown();
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				stringResponse[0] = response.body().string();
				latch.countDown();
			}
		});
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// TODO replace with more robust token storage solution
		Log.i(AlarmBuddyHttp.class.getName(), "Writing token to file");
		File file = new File(context.getExternalFilesDir(""), "token");

		FileOutputStream outputStream = new FileOutputStream(file);
		outputStream.write(stringResponse[0].getBytes());
		outputStream.close();
		System.out.println(file.getAbsolutePath());

		return stringResponse[0].substring(8, 12).equals("true") && stringResponse[0] != null;
	}
}






