package com.github.radm.theories.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;
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
import com.github.radm.theories.Pairwise;
import com.github.radm.theories.TheorySuite;
import com.github.radm.theories.WithConstraints;

/**
 * Sample test to demonstrate the use of
 * {@link com.github.radm.theories.TheorySuite} runner with the
 * {@link com.github.radm.theories.Constraint} annotation.
 */
@RunWith(TheorySuite.class)
@Pairwise
public class ConstraintsExampleTest {

	@DataPoints("century")
	public static List<Year> years = IntStream.range(2000, 2100).boxed().map(Year::of).collect(Collectors.toList());

	@DataPoints
	public static int[] fullMonthDays = IntStream.range(1, 31).toArray();

	/**
	 * A global constraint, with no name set.
	 */
	@Constraint
	public static boolean isEverValidDay(Month month, int monthDay) {
		return monthDay <= month.maxLength();
	}

	/**
	 * Another global constraint.
	 */
	@Constraint
	public static boolean isValidDay(Year year, Month month, int monthDay) {
		return isEverValidDay(month, monthDay) && monthDay <= year.atMonth(month).lengthOfMonth();
	}

	/**
	 * A named constraint
	 */
	@Constraint("weekday")
	public static boolean isWeekDay(DayOfWeek dayOfWeek) {
		return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
	}

	/**
	 * Another constraint with same name but different arguments
	 */
	@Constraint("weekday")
	public static boolean isWeekDay(Year year, Month month, int monthDay) {
		return isValidDay(year, month, monthDay) && isWeekDay(year.atMonth(month).atDay(monthDay).getDayOfWeek());
	}


	/**
	 * Global constraint on arguments of a test.
	 */
	@Theory
	public void yearDayRoundTrip(Year year, Month month, int monthDay) {

		// no assume needed because of global constraint
		int dayOfYear = year.atMonth(month).atDay(monthDay).getDayOfYear();

		assertTrue(dayOfYear > 0);
		assertTrue(dayOfYear <= 366);

		assertEquals(month, year.atDay(dayOfYear).getMonth());
		assertEquals(monthDay, year.atDay(dayOfYear).getDayOfMonth());

	}

	/**
	 * Simple constraint on one argument of a test.
	 * Could be done in other ways, but occasionally useful to restrict the domain of an enumeration or
	 * datapoint set.
	 */
	@Theory
	@WithConstraints({"weekday"})
	public void atLeastFourOfEachDayPerMonth(DayOfWeek dayOfWeek, Year year, Month month) {
		assertTrue(isWeekDay(dayOfWeek));
		YearMonth thisMonth = year.atMonth(month);

		long count = IntStream.range(1, thisMonth.lengthOfMonth()).boxed().map(thisMonth::atDay)
				.filter(ld -> ld.getDayOfWeek() == dayOfWeek).count();

		assertTrue(count >= 0);
	}

	@Theory
	@WithConstraints({"weekday"})
	public void mondayStartsWeek(Year year, Month month, int monthDay) {
		assertTrue(isWeekDay(year, month, monthDay));

		LocalDate day = year.atMonth(month).atDay(monthDay);

		if (day.getDayOfWeek() == DayOfWeek.MONDAY)
		{
			assertTrue(!isWeekDay(day.minusDays(1).getDayOfWeek()));
		}
		else
		{
			assertTrue(isWeekDay(day.minusDays(1).getDayOfWeek()));
		}

	}


}
