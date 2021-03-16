package edu.ust.alarmbuddy.common;

import at.favre.lib.crypto.bcrypt.BCrypt;
import at.favre.lib.crypto.bcrypt.BCrypt.Version;

public class AlarmBuddyBCrypt {
	public static Version HASH_FUNCTION = Version.VERSION_2A;
	public static int COST = 6;

	public static String hashPassword(String password) {
		return BCrypt.with(HASH_FUNCTION).hashToString(COST,password.toCharArray());
	}

	public static String hashPassword(String password, int cost) {
		return BCrypt.with(HASH_FUNCTION).hashToString(cost,password.toCharArray());
	}
}
