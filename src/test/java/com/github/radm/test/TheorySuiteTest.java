package com.github.radm.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.Theory;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import com.github.radm.TheorySuite;

/**
 * unit test for TheorySuite.
 * Covers more details than the ExampleTest
 *
 */
public class TheorySuiteTest {

	@RunWith(TheorySuite.class)
	public static class ValidTest {
		@Test
		public void simplePassingTest() {
			assertTrue(true);
		}
		@Test
		public void simpleFailingTest() {
            fail();
		}

		@Theory
		public void simplePassingTheory(boolean value) {
			assertTrue(value || !value);
		}
	}

	@Test
	public void simpleValidTest()  {
        Result result = JUnitCore.runClasses(ValidTest.class);
        assertEquals(4, result.getRunCount());
        assertEquals(1, result.getFailureCount());
        assertEquals(0, result.getIgnoreCount());

        assertTrue(result.getFailures().get(0).getException() instanceof AssertionError);

	}

	@RunWith(TheorySuite.class)
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
        Result result = JUnitCore.runClasses(BeforeAndAfter.class);
        assertEquals(1, result.getRunCount());
        assertEquals(0, result.getFailureCount());
        assertEquals(0, result.getIgnoreCount());
	}

	@RunWith(TheorySuite.class)
	public static class InvalidTest {
		@Test
		private int invalidTest() {
			return -1;
		}

	}

	@Test
	public void checksForInvalidTest()  {
        Result result = JUnitCore.runClasses(InvalidTest.class);
        assertEquals(3, result.getRunCount());
        assertEquals(3, result.getFailureCount());
        assertEquals(0, result.getIgnoreCount());

        result.getFailures().forEach(f -> assertTrue(f.toString(), f.toString().contains("ethod")));
	}
}
