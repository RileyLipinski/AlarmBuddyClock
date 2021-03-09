package edu.ust.alarmbuddy.ui.alarm;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import org.junit.Test;

public class AlarmPublisherTest {

	@Test
	public void setAlarmForToday() {
		// Fri Jan 01 12:00:00 CST 2021
		Calendar mockTime = Calendar.getInstance();
		mockTime.set(2021, 0, 1, 12, 0, 0);
		mockTime.set(Calendar.MILLISECOND, 0);

		// Fri Jan 01 13:00:00 CST 2021
		Calendar expectedAlarmTime = (Calendar) mockTime.clone();
		expectedAlarmTime.set(Calendar.HOUR_OF_DAY, 13);

		long actualAlarmTime = AlarmPublisher.wakeupTime(13, 0, mockTime.getTimeInMillis());

		assertEquals(expectedAlarmTime.getTimeInMillis(), actualAlarmTime);
	}

	@Test
	public void setAlarmForTomorrow() {
		// Fri Jan 01 12:00:00 CST 2021
		Calendar mockTime = Calendar.getInstance();
		mockTime.set(2021, 0, 1, 12, 0, 0);
		mockTime.set(Calendar.MILLISECOND, 0);

		// Sat Jan 02 11:00:00 CST 2021
		Calendar expectedAlarmTime = (Calendar) mockTime.clone();
		expectedAlarmTime.set(Calendar.HOUR_OF_DAY, 11);
		expectedAlarmTime.roll(Calendar.DAY_OF_YEAR, 1);

		long actualAlarmTime = AlarmPublisher.wakeupTime(11, 0, mockTime.getTimeInMillis());

		assertEquals(expectedAlarmTime.getTimeInMillis(), actualAlarmTime);
	}

	@Test
	public void setAlarmForTomorrowMonthRollover() {
		// Test month rollover
		// Sun Feb 28 12:00:00 CST 2021
		Calendar mockTime = Calendar.getInstance();
		mockTime.set(2021, 1, 28, 12, 0, 0);
		mockTime.set(Calendar.MILLISECOND, 0);

		// Mon Mar 01 11:00:00 CST 2021
		Calendar expectedAlarmTime = (Calendar) mockTime.clone();
		expectedAlarmTime.set(Calendar.HOUR_OF_DAY, 11);
		expectedAlarmTime.roll(Calendar.DAY_OF_YEAR, 1);

		long actualAlarmTime = AlarmPublisher.wakeupTime(11, 0, mockTime.getTimeInMillis());

		assertEquals(expectedAlarmTime.getTimeInMillis(), actualAlarmTime);
	}

	@Test
	public void setAlarmForTomorrowYearRollover() {
		// Test year rollover
		// Fri Dec 31 12:00:00 CST 2021
		Calendar mockTime = Calendar.getInstance();
		mockTime.set(2021, 11, 31, 12, 0, 0);
		mockTime.set(Calendar.MILLISECOND, 0);

		// Sat Jan 01 11:00:00 CST 2022
		Calendar expectedAlarmTime = (Calendar) mockTime.clone();
		expectedAlarmTime.set(Calendar.HOUR_OF_DAY, 11);
		expectedAlarmTime.roll(Calendar.DAY_OF_YEAR, 1);
		expectedAlarmTime.roll(Calendar.YEAR, 1);

		long actualAlarmTime = AlarmPublisher.wakeupTime(11, 0, mockTime.getTimeInMillis());

		assertEquals(expectedAlarmTime.getTimeInMillis(), actualAlarmTime);
	}
}
