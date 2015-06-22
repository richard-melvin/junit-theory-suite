package com.github.radm.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.Theory;
import org.junit.runner.Computer;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runners.model.RunnerBuilder;

import com.github.radm.TheorySuite;

/**
 * unit test for TheorySuite.
 * Covers more details than the ExampleTest
 *
 */
public class TheorySuiteTest {

	public static final Computer runSelect = new CompterSaysRunWithTheorySuite();

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


	public static class CompterSaysRunWithTheorySuite extends Computer {

		@Override
		protected Runner getRunner(RunnerBuilder builder, Class<?> testClass)
				throws Throwable {
			return new TheorySuite(testClass);
		}

	}
}
