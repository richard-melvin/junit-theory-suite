package com.github.radm.theories.pairwise;

import java.util.Arrays;
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

	Object currValue;
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
		} else {
			return null;
		}
	}


	@Override
	protected Object[] computeNext() {
		if (predicate == null)
		{
			return computeNextSimple();
		}
		return computeNextPassingPredicate();
	}


	protected Object[] computeNextPassingPredicate() {
		Object[] candidate = computeNextSimple();
		boolean requiresReset = false;
		while (!predicate.test(populateResult()) && !knownComplete) {
			if (ArgumentSet.LOG.isTraceEnabled()) {
				ArgumentSet.LOG.trace("Rejected {} by predicate on {}", Arrays.toString(populateResult()),
						args.argNames.get(argIndex));
			}
			if (valIter.hasNext()) {
				currValue = valIter.next();
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
	protected Object[] computeNextSimple() {

		if (nextColumn == null) {
			if (valIter.hasNext()) {
				currValue = valIter.next();
				return populateResult();
			}
			knownComplete = true;
			return null;
		}

		if (currValue == null && valIter.hasNext()) {
			currValue = valIter.next();
		}

		if (nextColumn.hasNext()) {

			return nextColumn.next();
		}
		while (valIter.hasNext()) {
			nextColumn.reset();
			currValue = valIter.next();
			if (nextColumn.hasNext()) {
				return nextColumn.next();
			}
		}

		knownComplete = true;
		return null;

	}

	private Object[] populateResult() {
		Object[] ret = new Object[argIndex + 1];

		ExhaustiveIterator it = this;
		for (int i = ret.length - 1; i >= 0; i--) {
			ret[i] = it.currValue;
			it = it.prevColumn;
		}

		return ret;
	}

	private void reset() {
		valIter = makeIter();
		knownComplete = false;
		currValue = null;
		nextValue = null;
		if (nextColumn != null) {
			nextColumn.reset();
		}
	}

}