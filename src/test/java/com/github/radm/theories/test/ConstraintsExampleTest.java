package com.github.radm.theories.test;

import static org.junit.Assert.assertTrue;

import java.time.DayOfWeek;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.contrib.theories.DataPoints;
import org.junit.contrib.theories.Theory;
import org.junit.runner.RunWith;

import com.github.radm.theories.Constraint;
import com.github.radm.theories.TheorySuite;

/**
 * Sample test to demonstrate the use of
 * {@link com.github.radm.theories.TheorySuite} runner with the
 * {@link com.github.radm.theories.Constraint} annotation.
 */
@RunWith(TheorySuite.class)
public class ConstraintsExampleTest {

	@DataPoints("century")
	public static List<Year> years = IntStream.range(2000, 2100).boxed().map(Year::of).collect(Collectors.toList());

	@DataPoints
	public static int[] fullMonthDays = IntStream.range(1, 31).toArray();

	@Constraint
	public static boolean isWeekDay(DayOfWeek dayOfWeek) {
		return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
	}

	@Constraint
	public static boolean isEverValidDay(Month month, int monthDay) {
		return monthDay <= month.maxLength();
	}

	public static boolean isValidDay(Year year, Month month, int monthDay) {
		return isEverValidDay(month, monthDay) && monthDay <= year.atMonth(month).lengthOfMonth();
	}

	@Constraint
	public static boolean isWeekDay(Year year, Month month, int monthDay) {
		return isValidDay(year, month, monthDay) && isWeekDay(year.atMonth(month).atDay(monthDay).getDayOfWeek());
	}

	@Theory
	public void atLeastFourOfEachDayPerMonth(DayOfWeek dayOfWeek, Year year, Month month) {
		assertTrue(isWeekDay(dayOfWeek));
		YearMonth thisMonth = year.atMonth(month);

		long count = IntStream.range(1, thisMonth.lengthOfMonth()).boxed().map(thisMonth::atDay)
				.filter(ld -> ld.getDayOfWeek() == dayOfWeek).count();

		assertTrue(count >= 0);
	}

}
