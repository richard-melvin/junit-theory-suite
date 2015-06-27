package com.github.radm.theories.test;

import static org.junit.Assert.assertTrue;

import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import org.junit.Test;
import org.junit.contrib.theories.DataPoints;
import org.junit.contrib.theories.Theory;
import org.junit.runner.notification.RunListener;

import com.github.radm.theories.Constraint;

/**
 * test the @Constraint annotation
 *
 */
public class ConstraintTest extends CustomRunnerTest {

	public abstract static class SimpleConstraints {

		@DataPoints
		public static int[] l1 = IntStream.range(0, 5).toArray();

		@DataPoints
		public static double[] d1 = DoubleStream.of(0, 1, 2).toArray();

		@Constraint
		public static boolean booleansTrue(boolean b) {
			return b;
		}

		@Constraint
		public static boolean doublesInOrder(double a, double b) {
			return a <= b;
		}
	}

	public static class OneArgument extends SimpleConstraints {

		@Theory
		public void oneArg(boolean b) {
			assertTrue(b);
		}
	}

	@Test
	public void simpleOneArgConstraint() throws Exception {

		RunListener listener = runTestWithMockListener(OneArgument.class);

		alwaysPassesWithCases(listener, 1);

	}

	public static class TwoArguments extends SimpleConstraints {

		@Theory
		public void twoArgs(boolean b1, boolean b2) {
			assertTrue(b1);
			assertTrue(b2);
		}
	}

	@Test
	public void simpleConstraintWithTwoArgs() throws Exception {

		RunListener listener = runTestWithMockListener(TwoArguments.class);

		alwaysPassesWithCases(listener, 1);

	}

	public static class ExcessArguments extends SimpleConstraints {

		@Theory
		public void manyArgs(int i1, boolean b1, int i2, boolean b2, int i3) {
			assertTrue(b1);
			assertTrue(b2);
		}

	}

	@Test
	public void simpleConstraintWithExtraArgs() throws Exception {

		RunListener listener = runTestWithMockListener(ExcessArguments.class);

		alwaysPassesWithCases(listener, 125);

	}



	public static class OnePair extends SimpleConstraints {

		@Theory
		public void inOrder(double a, double b) {
			assertTrue(a <= b);
		}

	}

	@Test
	public void onePairConstraint() throws Exception {

		RunListener listener = runTestWithMockListener(OnePair.class);

		alwaysPassesWithCases(listener, 6);

	}


	public static class TwoPairs extends SimpleConstraints {

		@Theory
		public void inOrder(double a, double b, double c) {
			assertTrue(a <= b);
			assertTrue(b <= c);

		}

	}

	@Test
	public void twoPairsConstraint() throws Exception {

		RunListener listener = runTestWithMockListener(TwoPairs.class);

		alwaysPassesWithCases(listener, 10);

	}


	public static class TwoPairsWithExtraArguments extends SimpleConstraints {

		@Theory
		public void inOrder(boolean b1, double a, double b, double c, int i) {
			assertTrue(a <= b);
			assertTrue(b <= c);

			assertTrue(b1);
		}

	}

	@Test
	public void twoPairsExtarArgsConstraint() throws Exception {

		RunListener listener = runTestWithMockListener(TwoPairsWithExtraArguments.class);

		alwaysPassesWithCases(listener, 10 * 5);

	}

}
