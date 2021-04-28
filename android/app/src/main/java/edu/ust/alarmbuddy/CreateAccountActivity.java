package edu.ust.alarmbuddy;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
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

		final TextView emailText = findViewById(R.id.emailText);
		final TextView firstNameText = findViewById(R.id.firstNameText);
		final TextView lastNameText = findViewById(R.id.lastNameText);
		final TextView phoneNumberText = findViewById(R.id.phoneNumberText);
		final TextView birthdayText = findViewById(R.id.birthdayText);
		final TextView usernameText = findViewById(R.id.usernameText);
		final TextView passwordText = findViewById(R.id.passwordText);
		final TextView confirmPasswordText = findViewById(R.id.confirmPasswordText);

		final EditText email = findViewById(R.id.email);
		final EditText firstName = findViewById(R.id.firstName);
		final EditText lastName = findViewById(R.id.lastName);
		final EditText phoneNumber = findViewById(R.id.phoneNumber);
		final EditText birthday = findViewById(R.id.birthday);
		final EditText username = findViewById(R.id.username);
		final EditText password = findViewById(R.id.password);
		final EditText confirmPassword = findViewById(R.id.confirmPassword);
		final TextView createAccountErrorText = findViewById(R.id.createAccountErrorText);

		// put all TextViews in an array
		TextView[] textViews = new TextView[]{emailText, firstNameText, lastNameText,
			phoneNumberText, birthdayText, usernameText, passwordText, confirmPasswordText};

		// put all EditTexts in an array
		EditText[] editTexts = new EditText[]{email, firstName, lastName,
			phoneNumber, birthday, username, password, confirmPassword};

		//text change listeners to reset text to black
		for (int i = 0; i < editTexts.length; i++) {
			int finalI = i;
			editTexts[i].addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {

				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					resetTextColor(editTexts[finalI]);
					resetTextColor(textViews[finalI]);
				}

				@Override
				public void afterTextChanged(Editable s) {

				}
			});
		}

		createAccountButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				boolean allFieldsValid = true;

				for (int i = 0; i < editTexts.length; i++) {
					if (editTexts[i].getText().toString().equals("")) {
						highlightInvalidField(textViews[i]);
					}
				}

				if (!isValidEmail(email.getText())) {
					highlightInvalidField(email);
					allFieldsValid = false;
					//createAccountErrorText.setText("Invalid email");
				}
				if (firstName.getText().toString().equals("")) {
					highlightInvalidField(firstName);
					allFieldsValid = false;
					//createAccountErrorText.setText("Please enter a first name");
				}
				if (lastName.getText().toString().equals("")) {
					highlightInvalidField(lastName);
					allFieldsValid = false;
					//createAccountErrorText.setText("Please enter a last name");
				}
				if (!isValidPhoneNumber(phoneNumber.getText().toString())) {
					highlightInvalidField(phoneNumber);
					allFieldsValid = false;
					//createAccountErrorText.setText("Invalid phone number");
				}
				if (!isValidBirthday(birthday.getText().toString())) {
					highlightInvalidField(birthday);
					allFieldsValid = false;
					//createAccountErrorText.setText("Invalid birthday");
				}
				if (!isValidUsername(username.getText().toString())) {
					highlightInvalidField(username);
					allFieldsValid = false;
					//createAccountErrorText.setText("Please enter a username");
				}
				if (password.getText().toString().equals("")) {
					highlightInvalidField(password);
					allFieldsValid = false;
					//createAccountErrorText.setText("Please enter a password");
				}
				if (confirmPassword.getText().toString().equals("")) {
					highlightInvalidField(confirmPassword);
					allFieldsValid = false;
					//createAccountErrorText.setText("Please confirm your password");
				}
				if (!password.getText().toString()
					.equals(confirmPassword.getText().toString())) {
					highlightInvalidField(confirmPassword);
					allFieldsValid = false;
					createAccountErrorText.setText("Passwords do not match");
				}
				if (allFieldsValid) {
					// reformat birthday for request (from MM-DD-YYYY to YYYY-MM-DD)
					String birthdate = birthday.getText().toString();
					birthdate = birthdate.substring(6, 10) + "-" + birthdate.substring(0, 2) + "-"
						+ birthdate.substring(3, 5);

					// make post request to create user
					if (AlarmBuddyHttp.createUser(username.getText().toString(),
						password.getText().toString(),
						firstName.getText().toString(),
						lastName.getText().toString(),
						email.getText().toString(),
						phoneNumber.getText().toString(),
						birthdate)) {
						// if account created successfully, inform user
						//createAccountErrorText
						//.setText("Account created, return to login page to login");

						moveToLogin();
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
	private static boolean isValidPhoneNumber(String phoneNumber) {
		String regexStr = "^([0-9]{3})[-]([0-9]{3})[-]([0-9]{4})";
		return phoneNumber.matches(regexStr);
	}

	private static boolean isValidBirthday(String birthday) {
		String regexStr = "^([0-9]{2})[-]?([0-9]{2})[-]?([0-9]{4})";
		// check format
		if (birthday.matches(regexStr)) {

			// reformat birthday to ISO date format (YYYYMMDD)
			birthday =
				birthday.substring(6, 10) + birthday.substring(0, 2) + birthday.substring(3, 5);
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

	/* name constraints:
	 * length between 1-100 characters, inclusive
	 * can only contain uppercase and lowercase English letters, hyphens, apostrophes
	 */
	private static boolean isValidName(String name) {
		String regex = "^[A-Za-z-']+$";

		return name.length() >= 1 && name.length() <= 100
			&& name.matches(regex);
	}

	/* username constraints:
	 * length between 5-20 characters, inclusive
	 * can only contain uppercase and lowercase English letters, numbers, underscores
	 * must be unique
	 */
	private static boolean isValidUsername(String username) {
		// TODO: check if username already taken
		String regex = "^[A-Za-z0-9_]+$";

		return username.length() >= 5 && username.length() <= 20
			&& username.matches(regex);
	}

	/* password constraints:
	 * length between 8-20 characters, inclusive
	 * can only contain uppercase and lowercase English letter, numbers,
	 *  and special characters but cannot contain <=>"&
	 * in ascii values, can contain 0x21-0x7E but cannot contain 0x3C, 0x3D, 0x3E,
	 *  0x22 and 0x26
	 * MUST contain at least one of each: uppercase letter, lowercase letter, number,
	 *  and special character
	 */
	private static boolean isValidPassword(String password) {
		boolean isValid = true;

		// check that password only contains valid characters
		for (int i = 0; i < password.length(); i++) {
			int ascii = (int) password.charAt(i);
			// out of range or is a forbidden character
			if (ascii < 33 || ascii > 126
				|| ascii == 60 || ascii == 61 || ascii == 62 || ascii == 34 || ascii == 38) {
				isValid = false;
			}
		}

		// check that password contains uppercase letter, lowercase letter, number, special character
		String regex = "(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!#$%'()*+,\\-./:;?`{|}~]])";

		return isValid && password.matches(regex);
	}

	private static void highlightInvalidField(EditText field) {
		field.setTextColor(Color.RED);
	}

	private static void highlightInvalidField(TextView field) {
		field.setTextColor(Color.RED);
	}

	private static void resetTextColor(EditText field) {
		if (field.getCurrentTextColor() != Color.BLACK) {
			field.setTextColor(Color.BLACK);
		}
	}

	private static void resetTextColor(TextView field) {
		if (field.getCurrentTextColor() != Color.BLACK) {
			field.setTextColor(Color.BLACK);
		}
	}

	private void moveToLogin() {
		startActivity(new Intent(this, LoginActivity.class));
	}

}