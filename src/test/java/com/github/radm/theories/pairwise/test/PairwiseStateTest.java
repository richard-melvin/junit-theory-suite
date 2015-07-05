package com.github.radm.theories.pairwise.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.github.radm.theories.pairwise.PairWiseState;

public class PairwiseStateTest {
	final static int[] twoBools = {2, 2};

	PairWiseState[] twoBoolState = {new PairWiseState(twoBools, 0), new PairWiseState(twoBools, 1)};

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
			pws.flagAsSelected(new int[]{0,0});
			pws.flagAsSelected(new int[]{0,1});
			pws.flagAsSelected(new int[]{1,0});
			pws.flagAsSelected(new int[]{1,1});
			assertTrue(pws.isComplete());

		}
	}


	@Test
	public void firstSelection() {

		assertEquals(0, twoBoolState[0].selectGiven(new int[] {0,0}));
		assertEquals(0, twoBoolState[1].selectGiven(new int[] {0,0}));

	}

	@Test
	public void secondSelection() {
		for (PairWiseState pws : twoBoolState)
		{
			pws.flagAsSelected(new int[]{0,0});
		}
		assertEquals(1, twoBoolState[0].selectGiven(new int[] {0,0}));
		assertEquals(1, twoBoolState[1].selectGiven(new int[] {1,0}));
	}


	@Test
	public void thirdSelection() {
		for (PairWiseState pws : twoBoolState)
		{
			pws.flagAsSelected(new int[]{0,0});
			pws.flagAsSelected(new int[]{1,1});

		}
		assertEquals(0, twoBoolState[0].selectGiven(new int[] {0,0}));
		assertEquals(1, twoBoolState[1].selectGiven(new int[] {0,0}));
	}


	@Test
	public void finalSelection() {
		for (PairWiseState pws : twoBoolState)
		{
			pws.flagAsSelected(new int[]{0,0});
			pws.flagAsSelected(new int[]{1,1});
			pws.flagAsSelected(new int[]{0,1});

		}
		assertEquals(1, twoBoolState[0].selectGiven(new int[] {0,0}));
		assertEquals(0, twoBoolState[1].selectGiven(new int[] {1,0}));
	}
}
