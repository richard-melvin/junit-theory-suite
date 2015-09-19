package com.github.radm.theories.pairwise.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import com.github.radm.theories.pairwise.PairWiseState;
import com.github.radm.theories.pairwise.SinglePairState;

@SuppressWarnings("javadoc")
public class PairwiseStateTest {
    final static int[] twoCols = { 2, 3 };

    SinglePairState onlyPair = new SinglePairState(0, 1, twoCols);
    PairWiseState[] twoBoolState = { new PairWiseState(twoCols, 0, Arrays.asList(onlyPair)),
            new PairWiseState(twoCols, 1, Arrays.asList(onlyPair)) };

    @Test
    public void initialState() {

        assertTrue(!onlyPair.isComplete());

    }

    @Test
    public void finalState() {

        onlyPair.select(0, 0);
        onlyPair.select(0, 1);
        onlyPair.select(0, 2);

        onlyPair.select(1, 0);
        onlyPair.select(1, 1);
        onlyPair.select(1, 2);

        assertTrue(onlyPair.isComplete());
    }

    @Test
    public void firstSelection() {

        assertEquals(0, twoBoolState[0].selectGiven(new int[] { -1, -1 }).get(0).intValue());
        assertEquals(0, twoBoolState[1].selectGiven(new int[] { 0, -1 }).get(0).intValue());

        assertTrue(!onlyPair.isComplete());

    }

    @Test
    public void secondSelection() {

        onlyPair.select(0, 0);

        assertTrue(!onlyPair.isComplete());

        assertEquals(1, twoBoolState[0].selectGiven(new int[] { -1, -1 }).get(0).intValue());
        assertEquals(1, twoBoolState[1].selectGiven(new int[] { 1, -1 }).get(0).intValue());
    }

    @Test
    public void thirdSelection() {

        onlyPair.select(0, 0);
        onlyPair.select(1, 1);

        assertTrue(!onlyPair.isComplete());

        assertEquals(0, twoBoolState[0].selectGiven(new int[] { -1, -1 }).get(0).intValue());
        assertEquals(2, twoBoolState[1].selectGiven(new int[] { 0, -1 }).get(0).intValue());
    }

    @Test
    public void finalSelection() {
        onlyPair.select(0, 0);
        onlyPair.select(1, 1);
        onlyPair.select(0, 1);
        onlyPair.select(0, 2);
        onlyPair.select(1, 0);
        assertTrue(!onlyPair.isComplete());

        assertEquals(1, twoBoolState[0].selectGiven(new int[] { -1, -1 }).get(0).intValue());
        assertEquals(2, twoBoolState[1].selectGiven(new int[] { 1, -1 }).get(0).intValue());
    }
}
