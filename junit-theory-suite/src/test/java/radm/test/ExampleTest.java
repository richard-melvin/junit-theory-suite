package radm.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
	public static int[] monthDays = IntStream.of(1, 31).toArray();

	@DataPoints
	public static List<Year> years = IntStream.of(1995, 2045).boxed().map(Year::of).collect(Collectors.toList());

	@Theory
	public void theoryOnYearAndMonth(Year year, Month month) throws Exception {

		assumeTrue(month != Month.FEBRUARY);
		LocalDate atEndOfMonth = year.atMonth(month).atEndOfMonth();
		assertEquals(month.maxLength(), atEndOfMonth.getDayOfMonth());

	}

	@Theory
	public void theoryOnYearAndWeekday(Year year, int monthDay, DayOfWeek day) throws Exception {

		YearMonth febForYear = year.atMonth(Month.FEBRUARY);
		assumeTrue(monthDay <= febForYear.lengthOfMonth());
		assumeTrue(febForYear.atDay(monthDay).getDayOfWeek() == day);

		assertTrue(febForYear.atDay(monthDay - 1).getDayOfWeek() == day.minus(1));

	}

}


