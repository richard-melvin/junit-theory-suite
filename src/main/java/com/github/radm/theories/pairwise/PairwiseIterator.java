package com.github.radm.theories.pairwise;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Pairwise coverage iteration, based on a variant of the algorithm specified in
 * <a href="http://goo.gl/CLYJ1d">Prioritized interaction testing for pair-wise
 * coverage with seeding and constraints</a>.
 *
 * @param <T>
 *            underlying common type or arguments, usually Object.
 */
public class PairwiseIterator<T> extends ArgSetIterator<T> {

	private final List<PairWiseState> columnStates = new ArrayList<>();
	private final SinglePairState[] cellStates;

	private final int tableSize;

	protected PairwiseIterator(ArgumentSet<T> args) {
		super(args);

		int[] argCounts = args.argsValues.stream().mapToInt(List::size).toArray();

		tableSize = argCounts.length;
		cellStates = new SinglePairState[tableSize * tableSize];

		for (int i = 0; i < tableSize; i++) {
			for (int j = 0; j < tableSize; j++) {
				if (i < j) {
					cellStates[getStateIndex(i, j)] = new SinglePairState(i, j, argCounts);
				}
			}
		}

		for (int i = 0; i < tableSize; i++) {
			List<SinglePairState> crossStates = new ArrayList<>(tableSize - 1);
			for (int j = 0; j < tableSize; j++) {
				if (i != j) {
					crossStates.add(cellStates[getStateIndex(i, j)]);
				}
			}

			columnStates.add(new PairWiseState(argCounts, i, crossStates));
		}

	}

	private int getStateIndex(int row, int col) {
		assert row != col;
		assert row < tableSize;
		assert col < tableSize;

		if (col < row) {
			return getStateIndex(col, row);
		}
		return col * tableSize + row;

	}

	@Override
	protected T[] computeNext() {

		if (isCoverageComplete()) {
			knownComplete = true;
			return null;
		}

		int[] selection = new int[tableSize];
		Arrays.setAll(selection, i -> -1);

		List<PairWiseState> updateOrder = new ArrayList<>(columnStates);
		updateOrder.sort(Comparator.comparingDouble(PairWiseState::globalDensity));

		for (PairWiseState pws : updateOrder) {
			selection[pws.getColumn()] = pws.selectGiven(selection);

		}
		for (int i = 0; i < selection.length; i++) {
			selection[i] = columnStates.get(i).selectGiven(selection);
		}

		for (SinglePairState sps : cellStates) {
			if (sps != null) {
				sps.select(selection[sps.colOne], selection[sps.colTwo]);
			}
		}

		return fillIn(selection);
	}

	private T[] fillIn(int[] selection) {

		@SuppressWarnings("unchecked")
		T[] ret = (T[]) new Object[selection.length];

		for (int i = 0; i < selection.length; i++) {
			ret[i] = args.argsValues.get(i).get(selection[i]);
		}
		return ret;
	}

	private boolean isCoverageComplete() {

		return columnStates.stream().allMatch(PairWiseState::isComplete);

	}

}
