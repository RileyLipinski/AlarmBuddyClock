package edu.ust.alarmbuddy.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme;
import androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme;
import androidx.security.crypto.MasterKey;
import androidx.security.crypto.MasterKey.KeyScheme;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
	 * @param context Application context used to generate MasterKey and decrypt SharedPreferences
	 *                file
	 * @param key     The key being searched for
	 *
	 * @return Whether the parameter key is stored in the preferences file, or false if an exception
	 * is thrown
	 */
	public static boolean containsKey(Context context, String key) {
		try {
			return getSharedPreferences(context).contains(key);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Fetches the value from the preferences file specified by the parameter key as a String. If
	 * the key does not have an associated value, returns null.
	 *
	 * @param context Application context used to generate MasterKey and decrypt SharedPreferences
	 *                file
	 * @param key     Key used to fetch data from the preferences file
	 *
	 * @return The preferences value associated with the parameter key, or null if the key does not
	 */
	@Nullable
	public static String getString(Context context, String key) {
		try {
			return getSharedPreferences(context).getString(key, null);
		} catch (GeneralSecurityException | IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Fetches the value from the preferences file specified by the parameter key as a String. If
	 * the key does not have an associated value, returns an empty String.
	 *
	 * @param context Application context used to generate MasterKey and decrypt SharedPreferences
	 *                file
	 * @param key     Key used to fetch data from the preferences file
	 *
	 * @return The preferences value associated with the parameter key, or an empty String if the
	 * key does not have an associated value.
	 */
	@NotNull
	public static String getStringNotNull(Context context, String key) {
		try {
			return getSharedPreferences(context).getString(key, "");
		} catch (GeneralSecurityException | IOException e) {
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * Fetches the value from the preferences file specified by the parameter key as an int. If the
	 * key is not stored, returns the parameter default value.
	 *
	 * @param context      Application context used to generate MasterKey and decrypt
	 *                     SharedPreferences file
	 * @param key          Key used to fetch data from the preferences file
	 * @param defaultValue The value to return if the key is not found in the SharedPreferences
	 *                     file
	 *
	 * @return The preferences value associated with the parameter key, or -1 if the key does not
	 * have an associated value.
	 *
	 * @throws GeneralSecurityException when the MasterKey cannot successfully decrypt the
	 *                                  preferences file
	 * @throws IOException              when the preferences file cannot be read
	 */
	public static int getInt(Context context, String key, int defaultValue) {
		try {
			return getSharedPreferences(context).getInt(key, defaultValue);
		} catch (GeneralSecurityException | IOException e) {
			e.printStackTrace();
			return defaultValue;
		}
	}

	/**
	 * Deletes the contents of the SharedPreferences file
	 *
	 * @param context Application context used to generate MasterKey and decrypt SharedPreferences
	 *                file
	 *
	 * @throws GeneralSecurityException when the MasterKey cannot successfully decrypt the
	 *                                  preferences file
	 * @throws IOException              when the preferences file cannot be read
	 */
	public static void clearSharedPreferences(Context context)
		throws GeneralSecurityException, IOException {
		getSharedPreferences(context).edit().clear().apply();
	}

	/**
	 * Prints the list of keys which are currently stored in the SharedPreferences file into the
	 * logs. If an exception occurs when retrieving the keyset, logs an error.
	 *
	 * @param context Application context used to generate MasterKey and decrypt SharedPreferences
	 *                file
	 */
	public static void printKeys(Context context) {
		try {
			Set<String> keys = getSharedPreferences(context).getAll().keySet();
			if (keys.isEmpty()) {
				Log.i(UserData.class.getName(), "UserData contains no keys.");
			} else {
				StringBuilder sb = new StringBuilder("UserData contains keys: ");
				for (String x : keys) {
					sb.append(x).append(", ");
				}
				sb.delete(sb.length() - 2, sb.length());
				Log.i(UserData.class.getName(), sb.toString());
			}
		} catch (GeneralSecurityException | IOException e) {
			Log.e(UserData.class.getName(), "ERROR OBTAINING KEYS");
			e.printStackTrace();
		}

	}
}
