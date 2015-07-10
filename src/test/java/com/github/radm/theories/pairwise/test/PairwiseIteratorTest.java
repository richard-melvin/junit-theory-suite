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
	public void expectedLength3() {
		assertTrue(9 == countArguments(threeIntegers));
	}

	@Test
	public void expectedLength4() {
		assertEquals(2, countArguments(twoBooleansConstrained));
	}

	@Test
	@Ignore
	public void expectedLength5() {
		assertTrue(11 >= countArguments(threeIntsConstrained));
	}

	@Test
	public void expectedLength6() {
		assertTrue(63 >= countArguments(fourDays));
	}

	@Test
	@Ignore
	public void expectedLength7() {
		assertTrue(64 >= countArguments(fourWeekDays));
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

	private int countArguments(ArgumentSet as) {
		Iterator<?> iter = as.pairwiseIterator();

		int count = 0;
		while (iter.hasNext()) {
			count++;
			Object[] next = (Object[]) iter.next();

			LOG.debug("got {}", Arrays.toString(next));
		}
		LOG.info("length of {} is {}", as.getArgNames(), count);
		return count;
	}

}
