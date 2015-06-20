package radm.test;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import radm.TheorySuite;

/**
 * Sample test to demonstrate the use of TheorySuite runner.
 *
 */
@RunWith(TheorySuite.class)
public class ExampleTest {

	@DataPoints
	public static int[] monthDays = IntStream.range(1, 31).toArray();

	@DataPoints
	public static List<Year> years = IntStream.range(1995, 1999).boxed()
			.map(Year::of).collect(Collectors.toList());

	@Test
	public void simpleTest() {
		assertTrue(Year.of(2012).isLeap());
	}

	@Theory
	public void theoryOnYearOnly(Year year) {
		assumeTrue(year.isLeap());

		assertEquals(Month.FEBRUARY.maxLength(), year.atMonth(Month.FEBRUARY)
				.lengthOfMonth());
	}

	@Theory
	public void theoryOnYearAndMonth(Year year, Month month) throws Exception {

		assumeTrue(month != Month.FEBRUARY);
		LocalDate atEndOfMonth = year.atMonth(month).atEndOfMonth();
		assertEquals(month.maxLength(), atEndOfMonth.getDayOfMonth());

	}

	@Theory
	public void theoryOnYearAndWeekday(Year year, int monthDay, DayOfWeek day)
			throws Exception {

		YearMonth febForYear = year.atMonth(Month.FEBRUARY);
		assumeTrue(monthDay <= febForYear.lengthOfMonth());
		assumeTrue(febForYear.atDay(monthDay).getDayOfWeek() == day);

		assertTrue(febForYear.atDay(monthDay).minusDays(1).getDayOfWeek() == day
				.minus(1));

	}

}
