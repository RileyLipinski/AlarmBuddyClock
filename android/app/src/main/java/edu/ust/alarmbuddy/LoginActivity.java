package edu.ust.alarmbuddy;

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
					if (h.authenticateLogin(stringUsername, stringPassword) && loginAttempts < 4) {
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
}






