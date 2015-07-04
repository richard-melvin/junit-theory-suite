package com.github.radm.theories.pairwise;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * Record the state of pairwise selection for a single column.
 *
 * @param <T>
 *            the generic type
 */
public class PairWiseState<T> {

	private final int columnNumber;
	private final ArgumentSet<T> argSet;
	private final int numColumnOptions;
	private final List<SinglePairState> states = new ArrayList<>();

	private class SinglePairState {
		final BitSet selectedPairs;
		final int[] selectionCount;
		int numSelected;
		final int numOptions;

		public SinglePairState(int otherColumns) {
			super();

			numOptions = argSet.argsValues.get(otherColumns).size();
			selectionCount = new int[numColumnOptions];
			selectedPairs = new BitSet(numColumnOptions * numOptions);
			numSelected = 0;
		}

		public void select(int otherColumn, int thisColumn) {
			int index = numOptions * otherColumn + thisColumn;

			if (!selectedPairs.get(index)) {
				numSelected++;
				selectionCount[thisColumn]++;
				selectedPairs.set(index);
			}
		}

		public boolean isSelected(int otherColumn, int thisColumn) {
			int index = numOptions * otherColumn + thisColumn;

			return selectedPairs.get(index);
		}

	}

	public PairWiseState(ArgumentSet<T> args, int columnNumber) {
		this.argSet = args;
		this.columnNumber = columnNumber;

		this.numColumnOptions = args.argsValues.get(columnNumber).size();

		for (int i = 0; i < args.argNames.size(); i++) {
			if (i != columnNumber) {
				states.add(new SinglePairState(i));
			}
		}
	}

	/**
	 * Access state, considering we left out the X=X case.
	 *
	 * @param column
	 *            number
	 * @return state for that column
	 */
	private SinglePairState getState(int col) {

		if (col < columnNumber) {
			return states.get(col);
		} else if (col > columnNumber) {
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

		int ret = -1;
		double bestWeight = -1;
		for (int selection = 0; selection < numColumnOptions; selection++) {

			double currWeight = calculateDensity(partialSelection, selection);
			if (currWeight > bestWeight) {
				ret = selection;
				bestWeight = currWeight;
			}
		}

		return ret;
	}

	private double calculateDensity(int[] partialSelection, int selection) {
		double density = 0;

		// calculate local density for fixed values
		for (int i = 0; i < columnNumber; i++)
		{
			SinglePairState state = getState(i);
			if (!state.isSelected(partialSelection[i], selection)) {
				density += (1.0 - state.numSelected / (double) state.numOptions);
			}
		}

		// calculate global density for fixed values
		for (int i = columnNumber + 1; i < partialSelection.length; i++)
		{
			SinglePairState state = getState(i);
			density += (1.0 - state.numSelected / (double) state.numOptions);

		}

		return density;
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
