package edu.ust.alarmbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import com.google.gson.JsonParser;
import edu.ust.alarmbuddy.common.AlarmBuddyHttp;
import edu.ust.alarmbuddy.common.UserData;
import edu.ust.alarmbuddy.ui.login.FailedLoginDialogFragment;
import edu.ust.alarmbuddy.ui.login.LoginViewModel;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
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
				try {
					if (authenticateLogin(stringUsername, stringPassword)
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

	@Override
	public void onStart() {
		String event = getIntent().getStringExtra("event");
		if(event != null) {
			if ("logout".equals(event)) {
				Toast.makeText(getApplicationContext(), "Successfully logged out.",
					Toast.LENGTH_SHORT).show();
			}
		}
		super.onStart();
	}

	private void loginToHome() {
		startActivity(new Intent(this, MainActivity.class));
	}

	private void moveToCreateAccount() {
		startActivity(new Intent(this, CreateAccountActivity.class));
	}

	private boolean authenticateLogin(String username, String password)
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

		String token = JsonParser.parseString(stringResponse[0])
			.getAsJsonObject()
			.get("token")
			.getAsString();

		try {
			UserData.getSharedPreferences(getApplicationContext()).edit()
				.putString("username", username)
				.putString("token", token)
				.apply();

		} catch (GeneralSecurityException e) {
			//TODO verify that this is acceptable behavior
			return false;
		}
		return stringResponse[0] != null && stringResponse[0].substring(8, 12).equals("true");
	}
}
