package com.github.radm.theories.pairwise.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import com.github.radm.theories.pairwise.PairWiseState;
import com.github.radm.theories.pairwise.SinglePairState;

public class PairwiseStateTest {
	final static int[] twoCols = {2, 3};

	SinglePairState onlyPair = new SinglePairState(0, 1, twoCols);
	PairWiseState[] twoBoolState = {new PairWiseState(twoCols, 0, Arrays.asList(onlyPair)), new PairWiseState(twoCols, 1, Arrays.asList(onlyPair))};

	@Test
	public void initialState() {

		for (PairWiseState pws : twoBoolState)
		{
			assertTrue(!pws.isComplete());
		}

	}


	@Test
	public void finalState() {

		for (PairWiseState pws : twoBoolState)
		{
			onlyPair.select(0, 0);
			onlyPair.select(0, 1);
			onlyPair.select(0, 2);

			onlyPair.select(1, 0);
			onlyPair.select(1, 1);
			onlyPair.select(1, 2);

			assertTrue(pws.isComplete());

		}
	}


	@Test
	public void firstSelection() {

		assertEquals(0, twoBoolState[0].selectGiven(new int[] {-1,-1}));
		assertEquals(0, twoBoolState[1].selectGiven(new int[] {0,-1}));

	}

	@Test
	public void secondSelection() {

		onlyPair.select(0, 0);

		assertEquals(1, twoBoolState[0].selectGiven(new int[] {-1,-1}));
		assertEquals(1, twoBoolState[1].selectGiven(new int[] {1,-1}));
	}


	@Test
	public void thirdSelection() {

		onlyPair.select(0, 0);
		onlyPair.select(1, 1);

		assertEquals(0, twoBoolState[0].selectGiven(new int[] {-1,-1}));
		assertEquals(2, twoBoolState[1].selectGiven(new int[] {0,-1}));
	}


	@Test
	public void finalSelection() {
		onlyPair.select(0, 0);
		onlyPair.select(1, 1);
		onlyPair.select(0, 1);
		onlyPair.select(0, 2);
		onlyPair.select(1, 0);

		assertEquals(1, twoBoolState[0].selectGiven(new int[] {-1,-1}));
		assertEquals(2, twoBoolState[1].selectGiven(new int[] {1,-1}));
	}
}
