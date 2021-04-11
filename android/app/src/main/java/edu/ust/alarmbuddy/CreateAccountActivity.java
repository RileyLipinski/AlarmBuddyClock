package edu.ust.alarmbuddy;

import android.content.Intent;
import android.icu.util.Calendar;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.os.Bundle;
import com.google.android.material.datepicker.CalendarConstraints;
import edu.ust.alarmbuddy.common.AlarmBuddyHttp;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class CreateAccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        final Button createAccountButton = findViewById(R.id.createAccountButton);
        final Button returnToLoginButton = findViewById(R.id.returnToLoginButton);
        final EditText email = findViewById(R.id.email);
        final EditText firstName = findViewById(R.id.firstName);
        final EditText lastName = findViewById(R.id.lastName);
        final EditText phoneNumber = findViewById(R.id.phoneNumber);
        final EditText birthday = findViewById(R.id.birthday);
        final EditText username = findViewById(R.id.username);
        final EditText password = findViewById(R.id.password);
        final EditText confirmPassword = findViewById(R.id.confirmPassword);
        final TextView createAccountErrorText = findViewById(R.id.createAccountErrorText);

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!isValidEmail(email.getText())) {
                    createAccountErrorText.setText("Invalid email");
                }
                else if (!isValidPhoneNumber(phoneNumber.getText().toString())) {
                    createAccountErrorText.setText("Invalid phone number");
                }
                else if (!isValidBirthday(birthday.getText().toString())) {
                    createAccountErrorText.setText("Invalid birthday");
                }
                else if (!password.getText().toString().equals(confirmPassword.getText().toString())) {
                    createAccountErrorText.setText("Passwords do not match");
                }
                else if(!isValidUsername(username.getText().toString())) {
                    createAccountErrorText.setText("Invalid username");
                }
                else if (!isValidPassword(password.getText().toString())) {
                    createAccountErrorText.setText("Invalid password");
                }

                else {
                    // reformat birthday for request (from MM-DD-YYYY to YYYY-MM-DD)
                    String birthdate = birthday.getText().toString();
                    birthdate = birthdate.substring(6, 10) + "-" + birthdate.substring(0, 2) + "-" + birthdate.substring(3, 5);

                    // make post request to create user
                    if (AlarmBuddyHttp.createUser(username.getText().toString(),
                            password.getText().toString(),
                            firstName.getText().toString(),
                            lastName.getText().toString(),
                            email.getText().toString(),
                            phoneNumber.getText().toString(),
                            birthdate)) {
                        // if account created successfully, inform user
                        createAccountErrorText.setText("Account created, return to login page to login");
                    } else {
                        // if request unsuccessful, show error text
                        createAccountErrorText.setText("Error: could not create user");
                    }
                }
            }
        });

        returnToLoginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                moveToLogin();
            }
        });

    }

    private static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    // since we are targeting a US audience, assume phone number matches US standards
    // regex format -> 9 digits, no spaces or punctuation allowed (eg. 1230003456)
    //              OR 9 digits separated by dashes (eg. 123-000-3456)
    private static boolean isValidPhoneNumber (String phoneNumber) {
        String regexStr = "^([0-9]{3})[-]([0-9]{3})[-]([0-9]{4})";
        return phoneNumber.matches(regexStr);
    }

    private static boolean isValidBirthday (String birthday) {
        String regexStr = "^([0-9]{2})[-]?([0-9]{2})[-]?([0-9]{4})";
        // check format
        if (birthday.matches(regexStr)) {

            // reformat birthday to ISO date format (YYYYMMDD)
            birthday = birthday.substring(6, 10) + birthday.substring(0, 2) + birthday.substring(3, 5);
            //check if valid date
            DateTimeFormatter dateFormatter = DateTimeFormatter.BASIC_ISO_DATE;
            try {
                LocalDate.parse(birthday, dateFormatter);
            } catch (DateTimeParseException e) {
                return false;
            }
            return true;
        }
        // if invalid format, return false
        return false;
    }

    private static boolean isValidUsername (String username) {
        // TODO: check if username already taken
        return true;
    }

    private static boolean isValidPassword (String password) {
        // TODO: add password constraints
        return true;
    }

    private void moveToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
    }

}