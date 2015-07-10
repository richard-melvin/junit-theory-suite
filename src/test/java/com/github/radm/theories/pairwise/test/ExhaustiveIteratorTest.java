package com.github.radm.theories.pairwise.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;

import org.junit.Test;
import org.junit.contrib.theories.Theory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.radm.theories.pairwise.ArgumentSet;

public class ExhaustiveIteratorTest extends ArgumentSetTest {

	private static final Logger LOG = LoggerFactory.getLogger(ExhaustiveIteratorTest.class);

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
		assertEquals(2, countArguments(twoBooleansConstrained));
	}

	@Test
	public void expectedLength5() {
		assertEquals(8, countArguments(threeIntsConstrained));
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
	public void canIterateWithoutGettingNull(ArgumentSet as) {
		for (Object[] args : as) {
			assertNotNull(args);
			for (Object o : args) {
				assertNotNull(o);
			}
		}
	}

	@Theory
	public void canCallHasNextFreely(ArgumentSet as) {

		Iterator<?> iter = as.iterator();

		while (iter.hasNext()) {
			assertTrue(iter.hasNext());
			iter.next();
		}
		assertTrue(!iter.hasNext());

	}


	protected int countArguments(ArgumentSet as) {
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
