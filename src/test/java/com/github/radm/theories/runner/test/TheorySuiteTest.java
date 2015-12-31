package com.github.radm.theories.runner.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.contrib.theories.DataPoints;
import org.junit.contrib.theories.FromDataPoints;
import org.junit.contrib.theories.Theory;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * unit test for TheorySuite. Covers more details than the ExampleTest
 *
 */
public class TheorySuiteTest extends CustomRunnerTest {

    public static class ValidTest {
        @Test
        public void simplePassingTest() {
            assertTrue(true);
        }

        @Theory
        public void simpleFailingTheory(boolean value) {
            assertTrue(value);
        }
    }

    @Test
    public void simpleValidTestResult() {

        Result result = JUnitCore.runClasses(runSelect, ValidTest.class);
        assertEquals(3, result.getRunCount());
        assertEquals(1, result.getFailureCount());
        assertEquals(0, result.getIgnoreCount());

        Failure failure = result.getFailures().get(0);
        assertTrue(failure.getException() instanceof AssertionError);

        assertTrue(failure.getDescription().getDisplayName().startsWith("simpleFailingTheory[false]"));

    }

    @Test
    public void simpleValidTestMocked() throws Exception {

        RunListener listener = runTestWithMockListener(ValidTest.class);

        ArgumentCaptor<Description> argument = ArgumentCaptor.forClass(Description.class);

        verify(listener, times(1)).testRunStarted(Mockito.any());
        verify(listener, times(3)).testStarted(argument.capture());
        verify(listener, times(3)).testFinished(Mockito.any());
        verify(listener, times(0)).testAssumptionFailure(Mockito.any());
        verify(listener, times(1)).testFailure(Mockito.any());

        for (Description d : argument.getAllValues()) {
            assertTrue(d.getTestClass().equals(ValidTest.class));
            assertTrue(d.getChildren().isEmpty());

        }

    }

    @Test
    public void simpleValidTestFilteredTestMethod() throws Exception {

        RunListener listener = runTestCaseWithMockListener(ValidTest.class,
                Description.createTestDescription(ValidTest.class, "simplePassingTest"));

        ArgumentCaptor<Description> argument = ArgumentCaptor.forClass(Description.class);

        verify(listener, times(1)).testRunStarted(Mockito.any());
        verify(listener, times(1)).testStarted(argument.capture());
        verify(listener, times(1)).testFinished(Mockito.any());
        verify(listener, times(0)).testAssumptionFailure(Mockito.any());
        verify(listener, times(0)).testFailure(Mockito.any());

        assertEquals("simplePassingTest", argument.getValue().getMethodName());
        assertEquals(ValidTest.class, argument.getValue().getTestClass());
        assertEquals(0, argument.getValue().getChildren().size());

    }

    @Test
    public void simpleValidTestFilteredTheory() throws Exception {

        RunListener listener = runTestCaseWithMockListener(ValidTest.class,
                Description.createTestDescription(ValidTest.class, "simpleFailingTheory[true]"));

        ArgumentCaptor<Description> argument = ArgumentCaptor.forClass(Description.class);

        verify(listener, times(1)).testRunStarted(Mockito.any());
        verify(listener, times(1)).testStarted(argument.capture());
        verify(listener, times(1)).testFinished(Mockito.any());
        verify(listener, times(0)).testAssumptionFailure(Mockito.any());
        verify(listener, times(0)).testFailure(Mockito.any());

        assertEquals("simpleFailingTheory[true]", argument.getValue().getMethodName());
        assertEquals(ValidTest.class, argument.getValue().getTestClass());
        assertEquals(0, argument.getValue().getChildren().size());

    }

    public static class BeforeAndAfter {

        private boolean ok = false;

        @Before
        public void flagAsOk() {
            ok = true;
        }

        @After
        public void flagAsNotOk() {
            ok = false;
        }

        @Test
        public void passIfOk() {
            assertTrue(ok);
        }

    }

    @Test
    public void beforeAndAfter() {
        Result result = JUnitCore.runClasses(runSelect, BeforeAndAfter.class);
        assertEquals(1, result.getRunCount());
        assertEquals(0, result.getFailureCount());
        assertEquals(0, result.getIgnoreCount());
    }

    public abstract static class InvalidTheory {
        @Theory
        private int invalidTest() {
            return -1;
        }

    }

    @Test
    public void checksForInvalidTheory() {
        Result result = JUnitCore.runClasses(runSelect, InvalidTheory.class);
        assertEquals(2, result.getRunCount());
        assertEquals(2, result.getFailureCount());
        assertEquals(0, result.getIgnoreCount());

        result.getFailures()
                .forEach(f -> assertTrue(f.toString(), f.toString().contains("Method invalidTest() should be")));
    }

    public static class FailAssumptionOrPass {
        @Theory
        public void booleanTheory(boolean value) {
            Assume.assumeTrue(value);
            assertTrue(value);
        }
    }

    @Test
    public void casesFailingAssumptionsAreCountedAsPasssed() throws Exception {

        RunListener listener = runTestWithMockListener(FailAssumptionOrPass.class);

        verify(listener, times(1)).testRunStarted(Mockito.any());
        verify(listener, times(2)).testStarted(Mockito.any());
        verify(listener, times(2)).testFinished(Mockito.any());
        verify(listener, times(1)).testAssumptionFailure(Mockito.any());
        verify(listener, never()).testFailure(Mockito.any());

    }

    public static class AssumptionAlwaysFails {
        @Theory
        public void booleanTheory(boolean value) {
            Assume.assumeTrue(value);
            Assume.assumeFalse(value);
        }
    }

    @Test
    public void failsWhenNoCasesSatisfyingAssumptions() throws Exception {

        RunListener listener = runTestWithMockListener(AssumptionAlwaysFails.class);

        verify(listener, times(2)).testStarted(Mockito.any());
        verify(listener, times(2)).testFinished(Mockito.any());
        verify(listener, times(2)).testAssumptionFailure(Mockito.any());
        verify(listener, times(1)).testFailure(Mockito.any());

    }

    public static class IgnoredTests {
        @Theory
        @Ignore
        public void booleanTheory(boolean value) {
            assertTrue(value);
        }

        @Test
        @Ignore
        public void ignored() {
            Assert.fail();
        }
    }

    @Test
    public void ignoredTestsIgnored() throws Exception {

        RunListener listener = runTestWithMockListener(IgnoredTests.class);

        verify(listener, times(0)).testStarted(Mockito.any());
        verify(listener, times(0)).testFinished(Mockito.any());
        verify(listener, times(0)).testAssumptionFailure(Mockito.any());
        verify(listener, times(0)).testFailure(Mockito.any());

    }

    public static class MissingDatapoints {
        @Theory
        public void intTheory(int value) {
            assertTrue(value > 0);
        }

        @Test
        public void passing() {
        }
    }

    @Test
    public void missingDataPointsFailsTest() throws Exception {

        RunListener listener = runTestWithMockListener(MissingDatapoints.class);

        verify(listener, times(1)).testStarted(Mockito.any());
        verify(listener, times(1)).testFinished(Mockito.any());
        verify(listener, times(0)).testAssumptionFailure(Mockito.any());
        verify(listener, times(1)).testFailure(Mockito.any());

    }

    public static class GenericsTest {

        @DataPoints
        public static List<Integer> l1 = IntStream.range(0, 23).boxed().collect(Collectors.toList());
        @DataPoints
        public static List<Long> l2 = LongStream.range(900, 923).boxed().collect(Collectors.toList());

        @Theory
        public void checkGenerics(int i, long l) {
            assertTrue(l > i);
        }
    }

    @Test
    public void canIdentifyArgsByGenericType() throws Exception {

        RunListener listener = runTestWithMockListener(GenericsTest.class);

        verify(listener, times(23 * 23)).testStarted(Mockito.any());
        verify(listener, times(23 * 23)).testFinished(Mockito.any());
        verify(listener, times(0)).testAssumptionFailure(Mockito.any());
        verify(listener, times(0)).testFailure(Mockito.any());

    }

    public static class NamedDataTest {

        @DataPoints("l1")
        public static List<Integer> l1 = IntStream.range(0, 25).boxed().collect(Collectors.toList());
        @DataPoints("l2")
        public static List<Integer> l2 = IntStream.range(400, 430).boxed().collect(Collectors.toList());

        @Theory
        public void checkGenerics(@FromDataPoints("l1") int i, @FromDataPoints("l2") int l) {
            assertTrue(l > i);
        }
    }

    @Test
    public void canIdentifyArgsByName() throws Exception {

        RunListener listener = runTestWithMockListener(NamedDataTest.class);

        verify(listener, times(25 * 30)).testStarted(Mockito.any());
        verify(listener, times(25 * 30)).testFinished(Mockito.any());
        verify(listener, times(0)).testAssumptionFailure(Mockito.any());
        verify(listener, times(0)).testFailure(Mockito.any());

    }

    public static class DatapointFunctionThrows {
        @Test
        public void passing() {
        }

        @Theory
        public void intTheory(int value) {
            assertTrue(value > 0);
        }

        @DataPoints
        public static List<Integer> lf1() {
            throw new NumberFormatException();
        }

    }

    @Test
    public void datapointFunctionThrowsException() throws Exception {

        RunListener listener = runTestWithMockListener(DatapointFunctionThrows.class);

        ArgumentCaptor<Failure> argument = ArgumentCaptor.forClass(Failure.class);

        verify(listener, times(1)).testFailure(argument.capture());

        assertTrue(argument.getValue().toString(), argument.getValue().getException() instanceof NumberFormatException);
    }

    public static class LargeNumberOfArgs {

        @DataPoints
        public static int[] l1 = IntStream.range(0, 5).toArray();

        @Theory
        public void sixArgsTheory(int i, boolean a, boolean b, boolean c, boolean d, int j) {
        }
    }

    @Test
    public void largeExhaustiveSearch() throws Exception {

        RunListener listener = runTestWithMockListener(LargeNumberOfArgs.class);

        alwaysPassesWithCases(listener, 5 * 5 * 2 * 2 * 2 * 2);

    }

    public static class StringArgs {

        @DataPoints
        public static String[] l1 = { "hello", "(/)" };

        @Theory
        public void stringArgsTheory(String s1, String s2, String s3) {
        }
    }

    @Test
    public void stringArgs() throws Exception {

        RunListener listener = runTestWithMockListener(StringArgs.class);

        alwaysPassesWithCases(listener, 2 * 2 * 2);

    }


    public static class TheoryWithNoArguments {
        @Theory
        public void hasNoArguments() {
        }

    }

    @Test
    public void runTheoryWithNoArgumentsLikeTest() throws Exception {
      RunListener listener = runTestWithMockListener(TheoryWithNoArguments.class);

      alwaysPassesWithCases(listener, 1);


    }


}
