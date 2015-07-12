package com.github.radm.theories.pairwise;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * Pairwise coverage iteration, based on a variant of the algorithm specified in
 * <a href="http://goo.gl/CLYJ1d">Prioritized interaction testing for pair-wise
 * coverage with seeding and constraints</a>.
 *
 * @param <T>
 *            underlying common type or arguments, usually Object.
 */
public class PairwiseIterator extends ArgSetIterator {

	private final List<PairWiseState> columnStates = new ArrayList<>();
	private final List<SinglePairState> cellStates;

	private final int tableSize;

	private final boolean hasConstraint;

	protected PairwiseIterator(ArgumentSet args) {
		super(args);

		int[] argCounts = args.argsValues.stream().mapToInt(List::size).toArray();

		tableSize = argCounts.length;
		cellStates = new ArrayList<>((tableSize * (tableSize - 1)) / 2);

		for (int i = 0; i < tableSize; i++) {
			for (int j = 0; j < tableSize; j++) {
				if (i < j) {
					cellStates.add(new SinglePairState(i, j, argCounts));
				}
			}
		}

		for (int i = 0; i < tableSize; i++) {
			final int col = i;
			List<SinglePairState> crossStates = new ArrayList<>(tableSize - 1);

			cellStates.stream().filter(cs -> cs.colOne == col || cs.colTwo == col).forEach(crossStates::add);

			columnStates.add(new PairWiseState(argCounts, i, crossStates));
		}

		hasConstraint = IntStream.range(0, tableSize).anyMatch(i -> args.getConstraint(i) != null);

		if (hasConstraint) {
			setupConstrainedCoverageTargets();
		}
	}

	@Override
	protected ArgVector computeNext() {

		if (isCoverageComplete()) {
			knownComplete = true;
			return null;
		}

		ArgVector selection = new ArgVector(args);

		List<PairWiseState> updateOrder = new ArrayList<>(columnStates);
		if (hasConstraint) {
			constrainedSelect(updateOrder, selection);
			if (knownComplete) {
				return null;
			}
		} else {
			updateOrder.sort(Comparator.comparingDouble(PairWiseState::globalDensity).reversed());
			for (PairWiseState pws : updateOrder) {
				selection.args[pws.getColumn()] = pws.selectGiven(selection.args).get(0);
			}
		}

		// we have a valid useful selection, so updates coverage state and return it.
		markAsCovered(selection);

		return selection;
	}

	private void markAsCovered(ArgVector selection) {
		for (SinglePairState sps : cellStates) {
			sps.select(selection.args[sps.colOne], selection.args[sps.colTwo]);
		}
	}

	/**
	 * o an exhaustive search from starting point until we find something passing constraints.
	 * @param updateOrder order in which to check columns
	 * @param selection empty selection
	 */
	private void constrainedSelect(List<PairWiseState> updateOrder, ArgVector selection) {

		final int size = updateOrder.size();
		int[] skipCounts = new int[size];
		for (int i = 0; i < size; i++) {
			final int col = i;
			PairWiseState pws = updateOrder.get(col);

			final List<Integer> sortedOptions = pws.selectGiven(selection.args);

			final Predicate<Object[]> constraint = args.getConstraint(col);
			if (constraint != null) {

				final Predicate<Integer> wrappedConstraint = selVal -> !constraint.test(selection.withValue(selVal, col).getArgVals());
				sortedOptions.removeIf(wrappedConstraint);
			}

			if (sortedOptions.size() > skipCounts[i]) {
				selection.args[pws.getColumn()] = sortedOptions.get(skipCounts[i]);
			}
			else {
				skipCounts[i] = 0;
				i--;
				if (i < 0) {
					knownComplete = true;
				}
				else {
					skipCounts[i]++;
				}
			}
		}
	}


	/**
	 * if constraints exist, we have to do an exhaustive iteration to
	 * get the target pairwise coverage statistics
	 */
	private void setupConstrainedCoverageTargets() {
		for (ArgVector av : args) {
			markAsCovered(av);
		}

		cellStates.forEach(cs -> cs.setAsHighWatermark());
	}


	private boolean isCoverageComplete() {

		return columnStates.stream().allMatch(PairWiseState::isComplete);

	}

}
