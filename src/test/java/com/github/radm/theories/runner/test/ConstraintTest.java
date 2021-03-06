package com.github.radm.theories.runner.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import org.junit.Test;
import org.junit.contrib.theories.DataPoints;
import org.junit.contrib.theories.Theory;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;

import com.github.radm.theories.Constraint;

/**
 * test the @Constraint annotation
 *
 */
@SuppressWarnings("javadoc")
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
        public void manyArgs( int i1, boolean b1, int i2, boolean b2, int i3) {
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

    public static class InvalidConstraint {

        @Constraint
        private int badConstraint() {
            return 0;
        }

        @Theory
        public void oneArg(boolean b) {
            assertTrue(b);
        }
    }

    @Test
    public void invalidConstraint() throws Exception {

        Result result = JUnitCore.runClasses(runSelect, InvalidConstraint.class);

        result.getFailures()
                .forEach(f -> assertTrue(f.toString(), f.toString().contains("Constraint method badConstraint")));

        assertEquals(4, result.getRunCount());
        assertEquals(4, result.getFailureCount());
        assertEquals(0, result.getIgnoreCount());

    }

    public static class Issue5Regression {
        @Constraint
        public static boolean isGreaterThan(Number n1, Number n2) {
            return n1.doubleValue() > n2.doubleValue();
        }

        @Theory
        public void greaterThan(Integer i1, Integer i2) {
            assertTrue(isGreaterThan(i1, i2));
        }

        @DataPoints
        public static int[] l1 = IntStream.range(0, 5).toArray();
    }

    @Test
    public void constraintOnBaseClass() throws Exception {

        RunListener listener = runTestWithMockListener(Issue5Regression.class);

        alwaysPassesWithCases(listener, 4 + 3 + 2 + 1);

    }

}
