package com.github.radm.theories.pairwise;

import java.util.ArrayList;
import java.util.List;

/**
 * Pairwise coverage iteration, based on a variant of the algorithm specified in
 * <a href="http://goo.gl/CLYJ1d">Prioritized interaction testing for pair-wise
 * coverage with seeding and constraints</a>.
 *
 * @param <T> underlying common type or arguments, usually Object.
 */
public class PairwiseIterator<T> extends ArgSetIterator<T>{

	List<PairWiseState> columnStates = new ArrayList<>();

	protected PairwiseIterator(ArgumentSet<T> args) {
		super(args);

		int[] argCounts = args.argsValues.stream().mapToInt(List::size).toArray();

		for (int i = 0; i < args.argNames.size(); i++) {
			columnStates.add(new PairWiseState(argCounts, i));
		}

	}

	@Override
	protected T[] computeNext() {

		if (isCoverageComplete()) {
			knownComplete = true;
			return null;
		}

		int[] selection = new int[args.argNames.size()];
		for (int i = 0; i < selection.length; i++) {
			selection[i] = columnStates.get(i).selectGiven(selection);
		}

		for (int i = 0; i < selection.length; i++) {
			columnStates.get(i).flagAsSelected(selection);
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
