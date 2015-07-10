package com.github.radm.theories.pairwise;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
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
	}

	@Override
	protected Object[] computeNext() {

		int[] selection = new int[tableSize];
		Arrays.setAll(selection, i -> -1);

		List<PairWiseState> updateOrder = new ArrayList<>(columnStates);
		updateOrder.sort(Comparator.comparingDouble(PairWiseState::globalDensity).reversed());

		if (hasConstraint) {
			constrainedSelect(updateOrder, selection);
			if (knownComplete) {
				return null;
			}
		} else {
			if (isCoverageComplete()) {
				knownComplete = true;
				return null;
			}
			for (PairWiseState pws : updateOrder) {
				selection[pws.getColumn()] = pws.selectGiven(selection).get(0);
			}
		}

		for (SinglePairState sps : cellStates) {
			sps.select(selection[sps.colOne], selection[sps.colTwo]);
		}

		return fillIn(selection);
	}

	private void constrainedSelect(List<PairWiseState> updateOrder, int[] selection) {
		// List<List<Integer>> sortedArgs = new ArrayList<>(tableSize);
		//
		// for (PairWiseState pws : updateOrder) {
		// final List<Integer> optionsByCoverage = pws.selectGiven(selection);
		// selection[pws.getColumn()] = optionsByCoverage.get(0);
		// sortedArgs.add(optionsByCoverage);
		// }
		//
		// ArgumentSet<Intege newArgs = new ArgumentSet<>(args.argNames,
		// sortedArgs);
		//
		// for (int i = 0; i < tableSize; i++) {
		// final Predicate<T[]> constraint = args.getConstraint(i);
		// if (constraint != null) {
		// final Predicate<Integer[]> wrappedConstraint = objs ->
		// constraint.test(fillIn(objs));
		//
		// newArgs.withConstraint(args.argNames.get(i), wrappedConstraint);
		// }
		// }
		//
		// Iterator<Integer[]> iterator = newArgs.iterator();
		// if (iterator.hasNext()) {
		// final Integer[] constrainedSelection = iterator.next();
		//
		// for (int i = 0; i < selection.length; i++) {
		// selection[i] = constrainedSelection[i];
		// }
		// } else {
		// knownComplete = true;
		// }
	}

	private Object[] fillIn(int[] selection) {

		Object[] ret = new Object[selection.length];

		for (int i = 0; i < selection.length; i++) {
			ret[i] = args.argsValues.get(i).get(selection[i]);
		}
		return ret;
	}

	private boolean isCoverageComplete() {

		return columnStates.stream().allMatch(PairWiseState::isComplete);

	}

}
