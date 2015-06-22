package com.github.radm.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.Theory;
import org.junit.runner.Computer;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunListener;
import org.junit.runners.model.RunnerBuilder;
import org.mockito.Mockito;

import com.github.radm.TheorySuite;

/**
 * unit test for TheorySuite.
 * Covers more details than the ExampleTest
 *
 */
public class TheorySuiteTest {

	public static final Computer runSelect = new Computer() {
		@Override
		protected Runner getRunner(RunnerBuilder builder, Class<?> testClass)
				throws Throwable {
			return new TheorySuite(testClass);
		}
	};

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
	public void simpleValidTest()  {

        Result result = JUnitCore.runClasses(runSelect, ValidTest.class);
        assertEquals(3, result.getRunCount());
        assertEquals(1, result.getFailureCount());
        assertEquals(0, result.getIgnoreCount());

        assertTrue(result.getFailures().get(0).getException() instanceof AssertionError);

	}

	public static class BeforeAndAfter {

		private boolean ok = false;
		@Before
		public void flagAsOk()
		{
			ok = true;
		}

		@After
		public void flagAsNotOk()
		{
			ok = false;
		}

		@Test
		public void passIfOk() {
			assertTrue(ok);
		}

	}

	@Test
	public void beforeAndAfter()  {
        Result result = JUnitCore.runClasses(runSelect, BeforeAndAfter.class);
        assertEquals(1, result.getRunCount());
        assertEquals(0, result.getFailureCount());
        assertEquals(0, result.getIgnoreCount());
	}

	public static class InvalidTest {
		@Theory
		private int invalidTest() {
			return -1;
		}

	}

	@Test
	public void checksForInvalidTest()  {
        Result result = JUnitCore.runClasses(runSelect, InvalidTest.class);
        assertEquals(1, result.getRunCount());
        assertEquals(1, result.getFailureCount());
        assertEquals(0, result.getIgnoreCount());

        result.getFailures().forEach(f -> assertTrue(f.toString(), f.toString().contains("ethod")));
	}


	public static class FailAssumptionOrPass {
		@Theory
		public void booleanTheory(boolean value) {
			Assume.assumeTrue(value);
			assertTrue(value);
		}
	}

	@Test
	public void casesFailingAssumptionsAreCountedAsPasssed() throws Exception  {

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
	public void failsWhenNoCasesSatisfyingAssumptions() throws Exception  {

		RunListener listener = runTestWithMockListener(AssumptionAlwaysFails.class);

        verify(listener, times(2)).testStarted(Mockito.any());
        verify(listener, times(2)).testFinished(Mockito.any());
        verify(listener, times(2)).testAssumptionFailure(Mockito.any());
        verify(listener, times(1)).testFailure(Mockito.any());

	}

	private RunListener runTestWithMockListener(Class<?> testCase) {
		RunListener listener = Mockito.mock(RunListener.class);
		JUnitCore core = new JUnitCore();
		core.addListener(listener);
		core.run(runSelect, testCase);
		return listener;
	}


}
