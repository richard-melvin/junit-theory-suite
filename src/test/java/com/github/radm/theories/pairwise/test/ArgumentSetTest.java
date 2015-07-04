package com.github.radm.theories.pairwise.test;

import java.time.DayOfWeek;
import java.util.Arrays;

import org.junit.contrib.theories.DataPoint;
import org.junit.contrib.theories.Theories;
import org.junit.runner.RunWith;

import com.github.radm.theories.pairwise.ArgumentSet;

/**
 * Common test data for all types of iterator.
 */
@RunWith(Theories.class)
public abstract class ArgumentSetTest {

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


	public static @DataPoint ArgumentSet<Boolean> twoBooleansConstrained = makeTwoBooleans()
			.withConstraint("a", args -> (Boolean) args[0]);

	public static @DataPoint ArgumentSet<Integer> threeIntsConstrained = makeThreeIntegers().withConstraint("c",
			args -> isOdd((Integer) args[0]) && isOdd((Integer) args[1]) && isOdd((Integer) args[2]));

	private static boolean isOdd(int i) {
		return i % 2 != 0;
	}

	private static boolean isWeekDay(DayOfWeek dayOfWeek) {
		return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
	}


}
