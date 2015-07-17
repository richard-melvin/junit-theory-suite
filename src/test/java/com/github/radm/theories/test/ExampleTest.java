package com.github.radm.theories.test;

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

import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.theories.DataPoints;
import org.junit.contrib.theories.Theory;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import com.github.radm.theories.TheorySuite;

/**
 * Sample test to demonstrate the use of {@link com.github.radm.theories.TheorySuite}
 * runner.
 */
@RunWith(TheorySuite.class)
@SuppressWarnings("javadoc")
public class ExampleTest {

  @Rule
  public TestName testName = new TestName();

  @DataPoints
  public static int[] fullMonthDays = IntStream.range(1, 31).toArray();

  @DataPoints("short")
  public static List<Year> years = IntStream.range(1995, 2001).boxed()
      .map(Year::of).collect(Collectors.toList());

  /**
   * Simple tests cases take no arguments.
   */
  @Test
  public void twentyTwelveIsLeapYear() {
    assertTrue(Year.of(2012).isLeap());
  }

  /**
   * Theories have arguments that get filled in from static members based on
   * annotations. By default, arguments are matched to datapoints based on
   * type.
   */
  @Theory
  public void leapYearsHaveMoreDaysInFebruary(Year year) {
    if (year.isLeap()) {
      assertEquals(Month.FEBRUARY.maxLength(),
          year.atMonth(Month.FEBRUARY).lengthOfMonth());
    } else {
      assertEquals(Month.FEBRUARY.minLength(),
          year.atMonth(Month.FEBRUARY).lengthOfMonth());
    }
  }

  /**
   * Theories can have multiple arguments; all possible combinations are
   * exercised. Enumerations and booleans don't need to be set up as
   * datapoints, given all possible values are to be used as inputs. Change
   * the assertion to use '==' to have it fail on one case out of 48.
   */
  @Theory
  public void endOfMonthAlwaysWithinRange(Year year, Month month) {

    LocalDate atEndOfMonth = year.atMonth(month).atEndOfMonth();

    assertTrue(month.maxLength() >= atEndOfMonth.getDayOfMonth());
    assertTrue(month.minLength() <= atEndOfMonth.getDayOfMonth());

  }

  /**
   * It is possible to discard datapoints for a particular test by using
   * {@link Assume.assumeTrue}.
   */
  @Theory
  public void previousDayBySubtraction(Year year, int monthDay) {

    YearMonth febForYear = year.atMonth(Month.FEBRUARY);
    assumeTrue(monthDay <= febForYear.lengthOfMonth());

    DayOfWeek day = febForYear.atDay(monthDay).getDayOfWeek();

    assertTrue(febForYear.atDay(monthDay).minusDays(1).getDayOfWeek() == day
        .minus(1));
  }

  /**
   * For two arguments of the same types, the same set of values is used for each.
   */
  @Theory
  public void yearOrderingMatchesDayOrdering(Year yearOne, Year yearTwo) {

    assumeTrue(!yearOne.equals(yearTwo));

    if (yearOne.compareTo(yearTwo) > 0)
    {
      assertTrue(yearOne.atDay(1).compareTo(yearTwo.atDay(1)) > 0);
      assertTrue(yearOne.atDay(1).compareTo(yearTwo.atMonth(Month.DECEMBER).atEndOfMonth()) > 0);
      assertTrue(yearOne.atMonth(Month.DECEMBER).atEndOfMonth().compareTo(yearTwo.atDay(1)) > 0);
    }
    else
    {
      assertTrue(yearOne.atDay(1).compareTo(yearTwo.atDay(1)) < 0);
      assertTrue(yearOne.atDay(1).compareTo(yearTwo.atMonth(Month.DECEMBER).atEndOfMonth()) < 0);
      assertTrue(yearOne.atMonth(Month.DECEMBER).atEndOfMonth().compareTo(yearTwo.atDay(1)) < 0);
    }
  }

  /**
   * The {@link org.junit.rules.TestName} rule can be used to find the name of
   * the currently running test. When the {@link com.github.radm.theories.TheorySuite}
   * runner is used in a theory, this name contains the argument values used
   * in the current test case.
   */
  @Theory
  public void theoryUsingTestName(DayOfWeek day) {

    assertTrue(testName.getMethodName().startsWith("theoryUsingTestName"));
    assertTrue(testName.getMethodName().contains(day.toString()));

  }

}
