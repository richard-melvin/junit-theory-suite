package com.github.radm.theories.pairwise;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Record the state of pairwise selection for a single column.
 *
 */
public class PairWiseState {
	private static final Logger LOG = LoggerFactory.getLogger(PairWiseState.class);

	private final int columnNumber;
	private final int[] argCounts;
	private final int numColumnOptions;
	private final List<SinglePairState> states = new ArrayList<>();

	private class SinglePairState {
		final BitSet selectedPairs;
		final int[] selectionCount;
		int numSelected;
		final int numOptions;

		public SinglePairState(int otherColumns) {
			super();

			numOptions = argCounts[otherColumns];
			selectionCount = new int[numColumnOptions];
			selectedPairs = new BitSet(numColumnOptions * numOptions);
			numSelected = 0;
		}

		public void select(int otherColumnValue, int thisColumnValue) {
			int index = numOptions * otherColumnValue + thisColumnValue;

			if (!selectedPairs.get(index)) {
				numSelected++;
				selectionCount[thisColumnValue]++;
				selectedPairs.set(index);
			}
		}

		public boolean isSelected(int otherColumnValue, int thisColumnValue) {
			int index = numOptions * otherColumnValue + thisColumnValue;

			return selectedPairs.get(index);
		}

	}

	public PairWiseState(int[] argCounts, int columnNumber) {
		this.argCounts = argCounts;
		this.columnNumber = columnNumber;

		this.numColumnOptions = argCounts[columnNumber];

		for (int i = 0; i < argCounts.length; i++) {
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
			return states.get(col - 1);
		}
		throw new AssertionError(col);
	}

	/**
	 * Checks if all required values have been selected.
	 *
	 * @return true, if is complete
	 */
	public boolean isComplete() {

		return states.stream().allMatch(sps -> sps.numSelected == sps.numOptions * numColumnOptions);
	}

	public int selectGiven(int[] partialSelection) {

		LOG.trace("select colummn {} given {}", columnNumber, partialSelection);

		int ret = -1;
		double bestWeight = -1;
		for (int selection = 0; selection < numColumnOptions; selection++) {

			double currWeight = calculateDensity(partialSelection, selection);
			if (currWeight > bestWeight) {
				ret = selection;
				bestWeight = currWeight;
			}
		}
		LOG.trace("selected {} for colummn {}", ret, columnNumber);

		return ret;
	}

	private double calculateDensity(int[] partialSelection, int selection) {
		double density = 0;

		// calculate local density for fixed values
		for (int i = 0; i < columnNumber; i++)
		{
			SinglePairState state = getState(i);
			final double target = (double) state.numOptions;
			if (!state.isSelected(partialSelection[i], selection)) {
				density += (1.0 - state.selectionCount[selection] / target);
				LOG.trace("extra density for new pair [{},{}] is {}", partialSelection[i], selection, density);

			}
		}

		// calculate global density for fixed values
		for (int i = columnNumber + 1; i < partialSelection.length; i++)
		{
			SinglePairState state = getState(i);
			final double target = (double) ( state.numOptions);

			density += (1.0 - state.selectionCount[selection] / target);

		}
		LOG.trace("density of {} is {}", selection, density);

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
