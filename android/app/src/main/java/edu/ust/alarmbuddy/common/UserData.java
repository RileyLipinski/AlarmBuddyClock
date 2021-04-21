package edu.ust.alarmbuddy.common;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme;
import androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme;
import androidx.security.crypto.MasterKey;
import androidx.security.crypto.MasterKey.KeyScheme;
import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * This class acts as a wrapper around the preferences file stored on the Android device. This
 * preferences file contains encrypted data about the logged-in user, including their auth token.
 */

public class UserData {

	public static final String FILENAME = "secretUserData";

	/**
	 * Decrypts the preferences file used for storing data about the logged in user and returns an
	 * associated SharedPreferences object.
	 *
	 * @param context Application context used to generate MasterKey
	 *
	 * @return A SharedPreferences instance that stores user data for the AlarmBuddy app
	 *
	 * @throws GeneralSecurityException when the MasterKey cannot successfully decrypt the
	 *                                  preferences file
	 * @throws IOException              when the preferences file cannot be read
	 */
	public static SharedPreferences getSharedPreferences(Context context)
		throws GeneralSecurityException, IOException {
		return EncryptedSharedPreferences.create(
			context,
			FILENAME,
			new MasterKey.Builder(context)
				.setKeyScheme(KeyScheme.AES256_GCM)
				.build(),
			PrefKeyEncryptionScheme.AES256_SIV,
			PrefValueEncryptionScheme.AES256_GCM
		);
	}

	/**
	 * Returns whether the parameter key is stored within the user's preferences file.
	 *
	 * @param context Application context used to generate MasterKey
	 * @param key     Key used to fetch data from the preferences file
	 *
	 * @return A boolean representing whether the parameter key is stored in the preferences file
	 *
	 * @throws GeneralSecurityException when the MasterKey cannot successfully decrypt the
	 *                                  preferences file
	 * @throws IOException              when the preferences file cannot be read
	 */
	public static boolean containsKey(Context context, String key)
		throws GeneralSecurityException, IOException {
		return getSharedPreferences(context).contains(key);
	}

	/**
	 * Fetches the value from the preferences file specified by the parameter key. If the key does
	 * not have an associated value, returns null.
	 *
	 * @param context Application context used to generate MasterKey
	 * @param key     Key used to fetch data from the preferences file
	 *
	 * @return The preferences value associated with the parameter key, or null if the key does not
	 * have an associated value.
	 *
	 * @throws GeneralSecurityException when the MasterKey cannot successfully decrypt the
	 *                                  preferences file
	 * @throws IOException              when the preferences file cannot be read
	 */
	public static String getString(Context context, String key)
		throws GeneralSecurityException, IOException {
		return getSharedPreferences(context).getString(key, null);
	}
}
