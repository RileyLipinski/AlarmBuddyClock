package edu.ust.alarmbuddy;

import static java.lang.Integer.parseInt;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import edu.ust.alarmbuddy.common.AlarmBuddyHttp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateAccountActivity extends AppCompatActivity {

	Button createAccountButton;
	Button returnToLoginButton;
	EditText email;
	EditText firstName;
	EditText lastName;
	EditText phoneNumber;
	EditText birthday;
	EditText username;
	EditText password;
	EditText confirmPassword;
	TextView emailText;
	TextView firstNameText;
	TextView lastNameText;
	TextView phoneNumberText;
	TextView birthdayText;
	TextView usernameText;
	TextView passwordText;
	TextView confirmPasswordText;
	TextView createAccountErrorText;
	EditText[] editTexts;
	TextView[] textViews;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_account);

		createAccountButton = findViewById(R.id.createAccountButton);
		returnToLoginButton = findViewById(R.id.returnToLoginButton);

		emailText = findViewById(R.id.emailText);
		firstNameText = findViewById(R.id.firstNameText);
		lastNameText = findViewById(R.id.lastNameText);
		phoneNumberText = findViewById(R.id.phoneNumberText);
		birthdayText = findViewById(R.id.birthdayText);
		usernameText = findViewById(R.id.usernameText);
		passwordText = findViewById(R.id.passwordText);
		confirmPasswordText = findViewById(R.id.confirmPasswordText);

		email = findViewById(R.id.email);
		firstName = findViewById(R.id.firstName);
		lastName = findViewById(R.id.lastName);
		phoneNumber = findViewById(R.id.phoneNumber);
		birthday = findViewById(R.id.birthday);
		username = findViewById(R.id.username);
		password = findViewById(R.id.password);
		confirmPassword = findViewById(R.id.confirmPassword);
		createAccountErrorText = findViewById(R.id.createAccountErrorText);

		// put all TextViews in an array
		textViews = new TextView[]{emailText, firstNameText, lastNameText,
			phoneNumberText, birthdayText, usernameText, passwordText, confirmPasswordText};

		// put all EditTexts in an array
		editTexts = new EditText[]{email, firstName, lastName,
			phoneNumber, birthday, username, password, confirmPassword};

	}

	// set listeners
	@Override
	public void onResume() {
		super.onResume();

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

		// special case to reset confirmPassword when password is changed
		password.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				resetTextColor(confirmPassword);

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		// listeners to delete error text on change
		birthday.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (createAccountErrorText.getText().toString()
					.equals("You must be at least 18 years old to create an account")) {
					createAccountErrorText.setText("");
				}
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		password.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (createAccountErrorText.getText().toString().contains("Password")) {
					createAccountErrorText.setText("");
				}
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		confirmPassword.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (createAccountErrorText.getText().toString().equals("Passwords do not match")) {
					createAccountErrorText.setText("");
				}
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		username.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (createAccountErrorText.getText().toString().contains("Username")) {
					createAccountErrorText.setText("");
				}
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		firstName.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (createAccountErrorText.getText().toString().contains("First name")) {
					createAccountErrorText.setText("");
				}
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		lastName.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (createAccountErrorText.getText().toString().contains("Last name")) {
					createAccountErrorText.setText("");
				}
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		createAccountButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (checkAllFields()) {
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

						Toast.makeText(CreateAccountActivity.this, "Account created successfully", 8).show();

						moveToLogin();
					} else {
						// if request unsuccessful, show error text
						createAccountErrorText.setText("Error: username, email, or phone number already in use");
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

	// returns whether or not all fields are valid
	// for all invalid fields, turns text red
	private boolean checkAllFields() {
		boolean allFieldsValid = true;

		for (int i = 0; i < editTexts.length; i++) {
			if (editTexts[i].getText().toString().equals("")) {
				highlightInvalidField(textViews[i]);
				allFieldsValid = false;
			}
		}

		// check all fields for their respective requirements
		if (!password.getText().toString()
				.equals(confirmPassword.getText().toString())) {
			highlightInvalidField(confirmPassword);
			allFieldsValid = false;
		}
		if (!passwordMeetsReqs(password.getText().toString())) {
			highlightInvalidField(password);
			createAccountErrorText.setText("Password must contain a lowercase letter, " +
					"uppercase letter, number, and special character (!#$%*)");
		}
		if (password.getText().toString().length() < 8 ||
				password.getText().toString().length() > 20) {
			highlightInvalidField(password);
			createAccountErrorText.setText("Password must be 8-20 characters in length");
		}
		if (!isValidPassword(password.getText().toString())) {
			highlightInvalidField(password);
			allFieldsValid = false;
		}
		if (username.getText().toString().length() > 0 &&
				!isValidUsername(username.getText().toString())) {
			highlightInvalidField(username);
			createAccountErrorText
					.setText("Username can only contain letters, numbers, and underscores");
			allFieldsValid = false;
		}
		if (!isValidBirthday(birthday.getText().toString())) {
			highlightInvalidField(birthday);
			allFieldsValid = false;
		} else if (!userIs18(birthday.getText().toString())) {
			highlightInvalidField(birthday);
			createAccountErrorText
					.setText("You must be at least 18 years old to create an account");
			allFieldsValid = false;
		}
		if (!isValidPhoneNumber(phoneNumber.getText().toString())) {
			highlightInvalidField(phoneNumber);
			allFieldsValid = false;
		}
		if (lastName.getText().toString().length() > 0 &&
				!isValidName(lastName.getText().toString())) {
			highlightInvalidField(lastName);
			createAccountErrorText
					.setText("Last name can only contain letters, apostrophes, and hyphens");
			allFieldsValid = false;
		}
		if (firstName.getText().toString().length() > 0 &&
				!isValidName(firstName.getText().toString())) {
			highlightInvalidField(firstName);
			createAccountErrorText
					.setText("First name can only contain letters, apostrophes, and hyphens");
			allFieldsValid = false;
		}
		if (!isValidEmail(email.getText())) {
			highlightInvalidField(email);
			allFieldsValid = false;
		}
		return allFieldsValid;
	}

	private boolean isValidEmail(CharSequence target) {
		return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
	}

	// since we are targeting a US audience, assume phone number matches US standards
	// must be in following format -> 9 digits, no spaces or punctuation allowed (eg. 1230003456)
	//              			   OR 9 digits separated by dashes (eg. 123-000-3456)
	private boolean isValidPhoneNumber(String phoneNumber) {
		String regexStr = "^([0-9]{3})[-]([0-9]{3})[-]([0-9]{4})";
		return phoneNumber.matches(regexStr);
	}

	// must be in MM-DD-YYYY format
	private boolean isValidBirthday(String birthday) {
		boolean isValid = true;
		String regexStr = "^([0-9]{2})[-]([0-9]{2})[-]([0-9]{4})";
		// check format
		if (birthday.matches(regexStr)) {
			//check if valid date
			SimpleDateFormat dateFormatter = new SimpleDateFormat("MM-dd-yyyy");
			dateFormatter.setLenient(false);
			try {
				Date date = dateFormatter.parse(birthday);
			} catch (ParseException e) {
				isValid = false;
			}

		} else {
			isValid = false;
		}

		return isValid;
	}

	// birthday must be at least 18 years prior to current date
	private boolean userIs18(String birthday) {
		// check that the user is at least 18 years old
		boolean isValid = true;
		SimpleDateFormat dateFormatter = new SimpleDateFormat("MM-dd-yyyy");
		int newYear = parseInt(birthday.substring(6)) + 18;
		String date = birthday.substring(0, 6) + newYear;
		try {
			Date newDate = dateFormatter.parse(date);
			Date currentDate = new Date();

			if (newDate.after(currentDate)) {
				isValid = false;
			}

		} catch (ParseException e) {
			isValid = false;
		}
		return isValid;
	}

	/* name constraints:
	 * length between 1-100 characters, inclusive
	 * can only contain uppercase and lowercase English letters, hyphens, apostrophes
	 */
	private boolean isValidName(String name) {
		String regex = "^[A-Za-z-']+$";

		return name.length() >= 1 && name.length() <= 100
			&& name.matches(regex);
	}

	/* username constraints:
	 * length between 5-20 characters, inclusive
	 * can only contain uppercase and lowercase English letters, numbers, underscores
	 * must be unique
	 */
	private boolean isValidUsername(String username) {
		// TODO: check if username already taken
		String regex = "^[A-Za-z0-9_]+$";

		return username.matches(regex);
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
	private boolean isValidPassword(String password) {
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
		return isValid;
	}

	// check that password contains uppercase letter, lowercase letter, number, special character
	private boolean passwordMeetsReqs(String password) {
		return password.matches(".*[A-Z].*") && password.matches(".*[a-z].*") &&
				password.matches(".*[0-9].*") && password.matches(".*[!#$%'()*+,\\-./:;?`{|}~\\]].*");
	}

	private void highlightInvalidField(EditText field) {
		field.setTextColor(Color.RED);
	}

	private void highlightInvalidField(TextView field) {
		field.setTextColor(Color.RED);
	}

	private void resetTextColor(EditText field) {
		if (field.getCurrentTextColor() != Color.BLACK) {
			field.setTextColor(Color.BLACK);
		}
	}

	private void resetTextColor(TextView field) {
		if (field.getCurrentTextColor() != Color.BLACK) {
			field.setTextColor(Color.BLACK);
		}
	}

	private void moveToLogin() {
		startActivity(new Intent(this, LoginActivity.class));
	}

}