package com.github.radm.theories.pairwise;

import java.util.Iterator;
import java.util.function.Predicate;

/**
 * Exhaustively iterate over the contents of an argset, while applying
 * constraints.
 */
public class ExhaustiveIterator extends ArgSetIterator {

	private Iterator<Object> valIter;
	private final ExhaustiveIterator nextColumn;
	private final ExhaustiveIterator prevColumn;

	private Object currValue;
	private int currIndex = -1;

	private final int argIndex;
	private final Predicate<Object[]> predicate;

	ExhaustiveIterator(ArgumentSet argumentSet) {
		this(argumentSet, null);
	}

	private ExhaustiveIterator(ArgumentSet argumentSet, ExhaustiveIterator prevColumn) {

		super(argumentSet);
		this.prevColumn = prevColumn;
		if (prevColumn == null) {
			argIndex = 0;
		} else {
			argIndex = prevColumn.argIndex + 1;
		}

		nextColumn = makeNextColumn();
		valIter = makeIter();

		predicate = args.getConstraint(argIndex);

	}

	private Iterator<Object> makeIter() {
		return args.argsValues.get(argIndex).iterator();
	}

	private ExhaustiveIterator makeNextColumn() {
		if (argIndex < args.argNames.size() - 1) {
			return new ExhaustiveIterator(args, this);
		}
    return null;
	}


	@Override
	protected ArgVector computeNext() {
		if (predicate == null)
		{
			return computeNextSimple();
		}
		return computeNextPassingPredicate();
	}


	protected ArgVector computeNextPassingPredicate() {
		ArgVector candidate = computeNextSimple();
		boolean requiresReset = false;
		while (!predicate.test(populateResult().getArgVals()) && !knownComplete) {
			if (ArgumentSet.LOG.isTraceEnabled()) {
				ArgumentSet.LOG.trace("Rejected {} by predicate on {}", populateResult(),
						args.argNames.get(argIndex));
			}
			if (valIter.hasNext()) {
				currValue = valIter.next();
				currIndex++;
			} else {
				knownComplete = true;
			}
			requiresReset = true;
		}
		if (requiresReset && !knownComplete) {
			if (nextColumn != null) {
				nextColumn.reset();
				candidate = computeNextSimple();
			} else {
				candidate = populateResult();
			}
		}
		return candidate;
	}

	/**
	 * copying a trick from Guava, combine next and hasNext into a single
	 * function with extra intermediate storage & a flag.
	 *
	 * @return next object, or null.
	 */
	protected ArgVector computeNextSimple() {

		if (nextColumn == null) {
			if (valIter.hasNext()) {
				currValue = valIter.next();
				currIndex++;
				return populateResult();
			}
			knownComplete = true;
			return null;
		}

		if (currValue == null && valIter.hasNext()) {
			currValue = valIter.next();
			currIndex++;
		}

		if (nextColumn.hasNext()) {

			return nextColumn.next();
		}
		while (valIter.hasNext()) {
			nextColumn.reset();
			currValue = valIter.next();
			currIndex++;
			if (nextColumn.hasNext()) {
				return nextColumn.next();
			}
		}

		knownComplete = true;
		return null;

	}

	private ArgVector populateResult() {
		ArgVector ret = new ArgVector(args);

		ExhaustiveIterator it = this;
		for (int i = argIndex; i >= 0; i--) {
			ret.args[i] = it.currIndex;
			it = it.prevColumn;
		}

		return ret;
	}

	private void reset() {
		valIter = makeIter();
		knownComplete = false;
		currValue = null;
		currIndex = -1;
		nextValue = null;
		if (nextColumn != null) {
			nextColumn.reset();
		}
	}

}