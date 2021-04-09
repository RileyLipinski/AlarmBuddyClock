package edu.ust.alarmbuddy;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.lifecycle.ViewModelProviders;
import edu.ust.alarmbuddy.ui.login.FailedLoginDialogFragment;
import edu.ust.alarmbuddy.ui.login.LoginViewModel;
import edu.ust.alarmbuddy.R;


public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    int loginAttempts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        LoginViewModel viewModel = ViewModelProviders.of(this).get(LoginViewModel.class);

        final Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // TODO: text comparison not working
                // get username/password from input
                TextView username = findViewById(R.id.textUsername);
                TextView password = findViewById(R.id.textPassword);
                // convert TextView to strings for comparison
                String stringUsername = username.getText().toString();
                String stringPassword = password.getText().toString();
                // if username and password match, "login" to homepage
                //Type "android" for username/password to see failed login attempt. Anything else will log you into the app.
                if (stringUsername.equals("android") && stringPassword.equals("android")  && loginAttempts < 4) {
                    //loginToHome(v);
                    FailedLoginDialogFragment dialog = new FailedLoginDialogFragment();
                    dialog.show(getSupportFragmentManager(), "ABC");
                }
                else {
                    loginAttempts++;
                    loginToHome(v);
                }
            }
        });

        //final Button forgotPasswordButton = findViewById(R.id.loginButton)
    }

    public void loginToHome(View view) {
        Intent homepage = new Intent(this, MainActivity.class);
        startActivity(homepage);
    }


}






