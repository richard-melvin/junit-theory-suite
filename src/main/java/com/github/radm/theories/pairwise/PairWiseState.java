package com.github.radm.theories.pairwise;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Record the state of pairwise selection for a single column.
 *
 */
public class PairWiseState {
	private static final Logger LOG = LoggerFactory.getLogger(PairWiseState.class);

	private final int column;
	final int numColumnOptions;
	private final List<SinglePairState> states;

	public PairWiseState(int[] argCounts, int columnNumber, List<SinglePairState> states) {
		this.column = columnNumber;

		this.numColumnOptions = argCounts[columnNumber];
		this.states = states;
	}

	/**
	 * Checks if all required values have been selected.
	 *
	 * @return true, if is complete
	 */
	public boolean isComplete() {

		return states.stream().allMatch(sps -> sps.isComplete());
	}

	public int selectGiven(int[] partialSelection) {

		LOG.trace("select colummn {} given {}", column, partialSelection);

		int ret = -1;
		double bestWeight = -1;
		for (int selection = 0; selection < numColumnOptions; selection++) {

			double currWeight = calculateDensity(partialSelection, selection);
			if (currWeight > bestWeight) {
				ret = selection;
				bestWeight = currWeight;
			}
		}
		LOG.trace("selected {} for colummn {}", ret, column);

		return ret;
	}

	public double globalDensity() {
		double density = 0;
		for (SinglePairState state : states) {

			for (int selection = 0; selection < numColumnOptions; selection++) {
				density += state.densityOf(column, selection);
			}
		}

		return density;
	}

	private double calculateDensity(int[] partialSelection, int selection) {
		double density = 0;

		for (SinglePairState state : states) {

			final int otherColumn = state.otherColumn(column);
			if (partialSelection[otherColumn] >= 0) {
				final int xSelection;
				final int ySelection;
				if (otherColumn > column) {
					xSelection = selection;
					ySelection = partialSelection[otherColumn];

				} else {
					xSelection = partialSelection[otherColumn];
					ySelection = selection;
				}

				if (!state.isSelected(xSelection, ySelection)) {
					LOG.trace("{}={}, {}={} is new", column, selection, otherColumn,
							partialSelection[otherColumn]);
					density += state.densityOf(column, selection);
				}
			} else {
				density += state.densityOf(column, selection);
			}
		}

		LOG.trace("density of {} is {}", selection, density);

		return density;
	}

	/**
	 * Gets the column.
	 *
	 * @return the column
	 */
	public int getColumn() {
		return column;
	}

}
