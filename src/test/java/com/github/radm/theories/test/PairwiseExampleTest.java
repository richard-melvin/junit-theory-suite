package com.github.radm.theories.test;

import java.time.DayOfWeek;
import java.time.Month;
import java.time.Year;

import org.junit.Rule;
import org.junit.contrib.theories.Theory;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import com.github.radm.theories.Pairwise;
import com.github.radm.theories.TheorySuite;
import com.github.radm.theories.WithConstraints;

/**
 * Sample test to demonstrate the use of {@link com.github.radm.theories.TheorySuite}
 * runner.
 */
@RunWith(TheorySuite.class)
public class PairwiseExampleTest extends ConstraintsExampleTest {

	@Rule
	public TestName testName = new TestName();


	/**
	 * Global constraint on arguments of a test.
	 */
	@Theory
	@Pairwise
	public void yearDayRoundTrip(Year year, Month month, int monthDay) {

		super.yearDayRoundTrip(year, month, monthDay);

	}

	/**
	 * Simple constraint on one argument of a test.
	 * Could be done in other ways, but occasionally useful to restrict the domain of an enumeration or
	 * datapoint set.
	 */
	@Theory
	@WithConstraints({"weekday"})
	@Pairwise
	public void atLeastFourOfEachDayPerMonth(DayOfWeek dayOfWeek, Year year, Month month) {
		super.atLeastFourOfEachDayPerMonth(dayOfWeek, year, month);
	}

	@Theory
	@Pairwise
	@WithConstraints({"weekday"})
	public void mondayStartsWeek(Year year, Month month, int monthDay) {
		super.mondayStartsWeek(year, month, monthDay);
	}



}
