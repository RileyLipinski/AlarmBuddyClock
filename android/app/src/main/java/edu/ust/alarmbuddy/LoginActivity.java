package edu.ust.alarmbuddy;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.lifecycle.ViewModelProviders;
import edu.ust.alarmbuddy.ui.LoginViewModel;
import edu.ust.alarmbuddy.R;


public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        LoginViewModel viewModel = ViewModelProviders.of(this).get(LoginViewModel.class);

        final Button button = findViewById(R.id.loginButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // TODO: text comparison not working
                // get username/password from input
                TextView username = findViewById(R.id.textUsername);
                TextView password = findViewById(R.id.textPassword);
                // convert TextView to strings for comparison
                String stringUsername = username.getText().toString();
                String stringPassword = password.getText().toString();
                // if username and password match, "login" to homepage
                if (stringUsername == "a" && stringPassword == "a") {
                    loginToHome(v);
                }

                loginToHome(v);
            }
        });
    }

    public void loginToHome(View view) {
        Intent homepage = new Intent(this, MainActivity.class);
        startActivity(homepage);
    }


}






