package edu.ust.alarmbuddy.ui.alarm;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import org.junit.Test;

public class AlarmFragmentTest {
	@Test
	public void setAlarmForToday() {
		// Fri Jan 01 12:00:00 CST 2021
		Calendar mockTimeOne = Calendar.getInstance();
		mockTimeOne.set(2021,0,1,12,0,0);
		mockTimeOne.set(Calendar.MILLISECOND,0);

		Calendar expectedAlarmTimeOne = (Calendar) mockTimeOne.clone();
		expectedAlarmTimeOne.set(Calendar.HOUR_OF_DAY,13);

		long actualAlarmTimeOne = AlarmFragment.wakeupTime(13,0,mockTimeOne.getTimeInMillis());

		assertEquals(expectedAlarmTimeOne.getTimeInMillis(),actualAlarmTimeOne);
	}

	@Test
	public void setAlarmForTomorrow() {
		// Fri Jan 01 12:00:00 CST 2021
		Calendar mockTimeOne = Calendar.getInstance();
		mockTimeOne.set(2021,0,1,12,0,0);
		mockTimeOne.set(Calendar.MILLISECOND,0);

		Calendar expectedAlarmTimeOne = (Calendar) mockTimeOne.clone();
		expectedAlarmTimeOne.set(Calendar.HOUR_OF_DAY,11);
		expectedAlarmTimeOne.set(Calendar.DAY_OF_MONTH,expectedAlarmTimeOne.get(Calendar.DAY_OF_MONTH)+1);

		long actualAlarmTimeOne = AlarmFragment.wakeupTime(11,0,mockTimeOne.getTimeInMillis());

		assertEquals(expectedAlarmTimeOne.getTimeInMillis(),actualAlarmTimeOne);

		// Test month rollover
		// Sun Feb 28 12:00:00 CST 2021
		Calendar mockTimeTwo = Calendar.getInstance();
		mockTimeTwo.set(2021,1,28,12,0,0);
		mockTimeTwo.set(Calendar.MILLISECOND,0);
		System.out.println(new Date(mockTimeTwo.getTimeInMillis()));

		Calendar expectedAlarmTimeTwo = (Calendar) mockTimeTwo.clone();
		expectedAlarmTimeTwo.set(Calendar.HOUR_OF_DAY,11);
		expectedAlarmTimeTwo.set(Calendar.DAY_OF_MONTH,expectedAlarmTimeTwo.get(Calendar.DAY_OF_MONTH)+1);
		System.out.println(new Date(expectedAlarmTimeTwo.getTimeInMillis()));

		long actualAlarmTimeTwo = AlarmFragment.wakeupTime(11,0,mockTimeTwo.getTimeInMillis());
		System.out.println(new Date(actualAlarmTimeTwo));

		assertEquals(expectedAlarmTimeTwo.getTimeInMillis(),actualAlarmTimeTwo);

		// Test year rollover
		// Sun Feb 28 12:00:00 CST 2021
		Calendar mockTimeThree = Calendar.getInstance();
		mockTimeThree.set(2021,11,31,12,0,0);
		mockTimeThree.set(Calendar.MILLISECOND,0);
		System.out.println(new Date(mockTimeThree.getTimeInMillis()));

		Calendar expectedAlarmTimeThree = (Calendar) mockTimeThree.clone();
		expectedAlarmTimeThree.set(Calendar.HOUR_OF_DAY,11);
		expectedAlarmTimeThree.set(Calendar.DAY_OF_MONTH,expectedAlarmTimeThree.get(Calendar.DAY_OF_MONTH)+1);
		System.out.println(new Date(expectedAlarmTimeThree.getTimeInMillis()));

		long actualAlarmTimeThree = AlarmFragment.wakeupTime(11,0,mockTimeThree.getTimeInMillis());
		System.out.println(new Date(actualAlarmTimeThree));

		assertEquals(expectedAlarmTimeThree.getTimeInMillis(),actualAlarmTimeThree);
	}
}
