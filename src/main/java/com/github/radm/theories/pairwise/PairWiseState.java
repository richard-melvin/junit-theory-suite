package com.github.radm.theories.pairwise;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

	/**
	 * Instantiates a new pair wise state.
	 *
	 * @param argCounts the arg counts
	 * @param columnNumber the column number
	 * @param states the states
	 */
	public PairWiseState(int[] argCounts, int columnNumber, List<SinglePairState> states) {
		this.column = columnNumber;

		this.numColumnOptions = argCounts[columnNumber];
		this.states = states;
	}

	/**
	 * Select options in weight order given a partial selection.
	 *
	 * @param partialSelection the partial selection
	 * @return the list
	 */
	public List<Integer> selectGiven(int[] partialSelection) {

		LOG.trace("select colummn {} given {}", column, partialSelection);

		double[] weights = new double[numColumnOptions];
		for (int selection = 0; selection < numColumnOptions; selection++) {

			weights[selection] = calculateDensity(partialSelection, selection);

		}

		List<Integer> ret = IntStream.range(0, numColumnOptions).boxed().collect(Collectors.toList());

		ret.sort(Comparator.comparing(i -> -weights[i]));

		if (LOG.isTraceEnabled()) {
			LOG.trace("selected {} for colummn {}", ret, column);
		}

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

		assert partialSelection[column] < 0;

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
					LOG.trace("{}={}, {}={} would be new if selected",
							column, selection, otherColumn,
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
