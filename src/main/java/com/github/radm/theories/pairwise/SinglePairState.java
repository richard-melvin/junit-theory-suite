package com.github.radm.theories.pairwise;

import java.util.BitSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * represent a single pair of columns in the pairwise coverage matrix.
 *
 * relies on the symmetry x,y <-> y,x
 */
public class SinglePairState {

	private static final Logger LOG = LoggerFactory.getLogger(SinglePairState.class);

	int numSelected;
	final BitSet selectedPairs;

	final int colOne;
	final int colTwo;

	final int[] selectionCountOne;
	final int[] selectionCountTwo;

	final int[] numOptions;

	public SinglePairState(int colOne, int colTwo, int[] numOptions) {
		super();

		assert colOne < colTwo;

		this.colOne = colOne;
		this.colTwo = colTwo;

		this.numOptions = numOptions;
		selectionCountOne = new int[numOptions[colOne]];
		selectionCountTwo = new int[numOptions[colTwo]];

		selectedPairs = new BitSet(numOptions[colOne] * numOptions[colTwo]);
		numSelected = 0;
	}

	public void select(int colOneValue, int colTwoValue) {
		int index = numOptions[colTwo] * colOneValue + colTwoValue;

		if (!selectedPairs.get(index)) {
			LOG.trace("{}={}, {}={} selected", colOne, colOneValue, colTwo, colTwoValue);

			numSelected++;
			selectionCountOne[colOneValue]++;
			selectionCountTwo[colTwoValue]++;
			selectedPairs.set(index);
		}
	}

	public boolean isSelected(int colOneValue, int colTwoValue) {
		int index = numOptions[colTwo] * colOneValue + colTwoValue;

		return selectedPairs.get(index);
	}

	/**
	 * X given y, or vice versa.
	 *
	 * @param colNo the column number
	 * @return the other column number
	 */
	int otherColumn (int colNo) {
		if (colNo == colOne) {
			return colTwo;
		}
		if (colNo == colTwo) {
			return colOne;
		}
		throw new AssertionError(colNo);

	}

	/**
	 * calculate a metric of the number of pairs needing covering
	 *
	 * @param thisColumnValue
	 *            value for this column
	 * @return metric
	 */
	public double densityOf(int colNo, int colValue) {

		if (colNo == colOne) {
			return densityOne(colValue);
		}
		if (colNo == colTwo) {
			return densityTwo(colValue);
		}
		throw new AssertionError(colNo);
	}

	private double densityOne(int colOneValue) {
		final double target = (double) numOptions[colTwo];
		return (1.0 - selectionCountOne[colOneValue] / target);
	}

	private double densityTwo(int colTwoValue) {
		final double target = (double) numOptions[colOne];
		return (1.0 - selectionCountTwo[colTwoValue] / target);
	}

	public boolean isComplete() {
		return numSelected >= numOptions[colOne] * numOptions[colTwo];
	}

	/**
	 * Flag everything not currently present as unreachable.
	 *
	 */
	public void setAsHighWatermark() {

		numSelected = (numOptions[colOne] * numOptions[colTwo]) - numSelected ;
		for (int i = 0; i < selectionCountOne.length; i++) {
			selectionCountOne[i] = numOptions[colTwo] - selectionCountOne[i];
		}
		for (int i = 0; i < selectionCountTwo.length; i++) {
			selectionCountTwo[i] = numOptions[colOne] - selectionCountTwo[i];
		}

		selectedPairs.flip(0, selectedPairs.size());

	}
}