package com.github.radm.theories.pairwise.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Test;
import org.junit.contrib.theories.Theories;
import org.junit.runner.RunWith;

import com.github.radm.theories.pairwise.ArgVector;
import com.github.radm.theories.pairwise.ArgumentSet;

/**
 * unit test of ArgumentSet. Note uses old theories runner to be safe.
 */
@RunWith(Theories.class)
public class PairwiseIteratorTest extends ArgumentSetTest {

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
	public void expectedLength5() {
		assertEquals(5, countArguments(threeIntsConstrained));
	}

	@Test
	public void expectedLength6() {
		assertEquals(63, countArguments(fourDays));
	}

	@Test
	public void expectedLength7() {
		assertEquals(36, countArguments(fourWeekDays));
	}



	private int countArguments(ArgumentSet as) {
		Iterator<ArgVector> iter = as.pairwiseIterator();

		return countByIterator(as, iter);
	}

}
