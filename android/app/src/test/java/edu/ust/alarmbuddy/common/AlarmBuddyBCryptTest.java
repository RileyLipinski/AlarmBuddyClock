package edu.ust.alarmbuddy.common;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.junit.Test;

public class AlarmBuddyBCryptTest {

	@Test
	public void hashPasswordTest() {
		String result = AlarmBuddyBCrypt.hashPassword("1234");
		System.out.println(AlarmBuddyBCrypt.hashPassword("12345"));
		System.out.println(result);
		assert(BCrypt.verifyer().verify("1234".toCharArray(),result.toCharArray()).verified);
	}
}