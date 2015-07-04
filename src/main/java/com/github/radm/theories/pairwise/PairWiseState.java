package com.github.radm.theories.pairwise;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * Record the state of pairwise selection for a single column.
 *
 * @param <T> the generic type
 */
public class PairWiseState<T> {

	private final int columnNumber;
	private final ArgumentSet<T> argSet;
	private final int numColumnOptions;
	private final List<SinglePairState> states = new ArrayList<>();


	private class SinglePairState {
		final BitSet selectedPairs;
		int numSelected;
		final int numOptions;

		public SinglePairState(int columnNumber) {
			super();

			numOptions = argSet.argsValues.get(columnNumber).size();
			selectedPairs = new BitSet(numColumnOptions * numOptions);
			numSelected = 0;
		}

		public void select(int x, int y) {
			int index = numOptions * x + y;

			if (!selectedPairs.get(index)) {
				numSelected++;
				selectedPairs.set(index);
			}
		}

	}


	public PairWiseState(ArgumentSet<T> args, int columnNumber) {
		this.argSet = args;
		this.columnNumber = columnNumber;

		this.numColumnOptions = args.argsValues.get(columnNumber).size();

		for (int i = 0; i < args.argNames.size(); i++)		{
			if (i != columnNumber) {
				states.add(new SinglePairState(i));
			}
		}
	}

	/**
	 * Access state, considering we left out the X=X case.
	 * @param column number
	 * @return state for that column
	 */
	private SinglePairState getState(int col) {

		if (col < columnNumber) {
			return states.get(col);
		}
		else if (col > columnNumber) {
			return states.get(col);
		}
		throw new AssertionError(col);
	}

	/**
	 * Checks if all required values have been selected.
	 *
	 * @return true, if is complete
	 */
	public boolean isComplete() {

		return states.stream().anyMatch(sps -> sps.numSelected < sps.numOptions * numColumnOptions);
	}


	public int selectGiven(int[] partialSelection) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void flagAsSelected(int[] completeSelection) {

		for (int i = 0; i < completeSelection.length; i++) {

			if (i != columnNumber) {
				SinglePairState state = getState(i);

				state.select(completeSelection[i], completeSelection[columnNumber]);
			}
		}
	}
}
