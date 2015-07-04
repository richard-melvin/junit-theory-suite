package com.github.radm.theories.pairwise.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;

import org.junit.Ignore;
import org.junit.Test;
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
public class PairwiseIteratorTest extends ArgumentSetTest {

	private static final Logger LOG = LoggerFactory.getLogger(PairwiseIteratorTest.class);

	@Test
	public void expectedLength1() {
		assertEquals(2, countArguments(oneBoolean));
	}

	@Test
	public void expectedLength2() {
		assertEquals(4, countArguments(twoBooleans));
	}

	@Test
	@Ignore
	public void expectedLength3() {
		assertEquals(9, countArguments(threeIntegers));
	}

	@Test
	public void expectedLength4() {
		assertEquals(2, countArguments(twoBooleansConstrained));
	}

	@Test
	@Ignore
	public void expectedLength5() {
		assertEquals(8, countArguments(threeIntsConstrained));
	}

	@Test
	@Ignore
	public void expectedLength6() {
		assertEquals(7 * 7, countArguments(fourDays));
	}

	@Test
	@Ignore
	public void expectedLength7() {
		assertEquals(5 * 5, countArguments(fourWeekDays));
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
		Iterator<?> iter = as.pairwiseIterator();

		int count = 0;
		while (iter.hasNext()) {
			count++;
			Object[] next = (Object[]) iter.next();

			LOG.debug("got {}", Arrays.toString(next));
		}
		return count;
	}

}
