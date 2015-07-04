package com.github.radm.theories.pairwise;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.Iterator;

import org.junit.Test;
import org.junit.contrib.theories.DataPoint;
import org.junit.contrib.theories.Theories;
import org.junit.contrib.theories.Theory;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.radm.theories.pairwise.ArgumentSet;

/**
 * unit test of ArgumentSet. Note uses old theories runner to be safe.
 */
@RunWith(Theories.class)
public class ArgumentSetTest {

	private static final Logger LOG = LoggerFactory.getLogger(ArgumentSetTest.class);

	public static @DataPoint ArgumentSet<Boolean> oneBoolean = new ArgumentSet<>(Arrays.asList("a"),
			Arrays.asList(Arrays.asList(true, false)));

	public static @DataPoint ArgumentSet<Boolean> twoBooleans = makeTwoBooleans();

	private static ArgumentSet<Boolean> makeTwoBooleans() {
		return new ArgumentSet<>(Arrays.asList("a", "b"),
				Arrays.asList(Arrays.asList(true, false), Arrays.asList(false, true)));
	}

	public static @DataPoint ArgumentSet<Integer> threeIntegers = makeThreeIntegers();

	private static ArgumentSet<Integer> makeThreeIntegers() {
		return new ArgumentSet<>(Arrays.asList("a", "b", "c"),
				Arrays.asList(Arrays.asList(1, 2, 3), Arrays.asList(1, 2, 3), Arrays.asList(1, 2, 3)));
	}

	public static @DataPoint ArgumentSet<DayOfWeek> fourDays = makeFourDays();

	private static ArgumentSet<DayOfWeek> makeFourDays() {
		return ArgumentSet.fromArray(Arrays.asList("a", "b", "c", "d"),
				Arrays.asList(DayOfWeek.values(), DayOfWeek.values(),
						DayOfWeek.values(), DayOfWeek.values()));
	}

	public static @DataPoint ArgumentSet<DayOfWeek> fourWeekDays = makeFourDays()
			.withConstraint("a", args -> isWeekDay((DayOfWeek) args[0]))
			.withConstraint("b", args -> isWeekDay((DayOfWeek) args[1]))
			.withConstraint("c", args -> isWeekDay((DayOfWeek) args[2]))
			.withConstraint("d", args -> isWeekDay((DayOfWeek) args[3]));


	public static @DataPoint ArgumentSet<Boolean> twoBooleansContrained = makeTwoBooleans()
			.withConstraint("a", args -> (Boolean) args[0]);

	public static @DataPoint ArgumentSet<Integer> threeIntsContrained = makeThreeIntegers().withConstraint("c",
			args -> isOdd((Integer) args[0]) && isOdd((Integer) args[1]) && isOdd((Integer) args[2]));

	private static boolean isOdd(int i) {
		return i % 2 != 0;
	}

	private static boolean isWeekDay(DayOfWeek dayOfWeek) {
		return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
	}

	@Test
	public void expectedLength1() {
		assertEquals(2, countArguments(oneBoolean));
	}

	@Test
	public void expectedLength2() {
		assertEquals(4, countArguments(twoBooleans));
	}

	@Test
	public void expectedLength3() {
		assertEquals(27, countArguments(threeIntegers));
	}

	@Test
	public void expectedLength4() {
		assertEquals(2, countArguments(twoBooleansContrained));
	}

	@Test
	public void expectedLength5() {
		assertEquals(8, countArguments(threeIntsContrained));
	}

	@Test
	public void expectedLength6() {
		assertEquals(7 * 7 * 7 * 7, countArguments(fourDays));
	}

	@Test
	public void expectedLength7() {
		assertEquals(5 * 5 * 5 * 5, countArguments(fourWeekDays));
	}


	@Theory
	public void canIterateWithoutGettingNull(ArgumentSet<? extends Object> as) {
		for (Object[] args : as) {
			assertNotNull(args);
			for (Object o : args) {
				assertNotNull(o);
			}
		}
	}

	@Theory
	public void canCallHasNextFreely(ArgumentSet<? extends Object> as) {

		Iterator<?> iter = as.iterator();

		while (iter.hasNext()) {
			assertTrue(iter.hasNext());
			iter.next();
		}
		assertTrue(!iter.hasNext());

	}

	private int countArguments(ArgumentSet<? extends Object> as) {
		Iterator<?> iter = as.iterator();

		int count = 0;
		while (iter.hasNext()) {
			count++;
			Object[] next = (Object[]) iter.next();

			LOG.debug("got {}", Arrays.toString(next));
		}
		return count;
	}

}
