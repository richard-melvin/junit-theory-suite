package com.github.radm.theories.pairwise;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Predicate;

/**
 * Exhaustively iterate over the contents of an argset, while applying
 * constraints.
 */
public class ExhaustiveIterator<T> extends ArgSetIterator<T> {

	private Iterator<? extends T> valIter;
	private final ExhaustiveIterator<T> nextColumn;
	private final ExhaustiveIterator<T> prevColumn;

	T currValue;
	private final int argIndex;
	private final Predicate<Object[]> predicate;

	ExhaustiveIterator(ArgumentSet<T> argumentSet) {
		this(argumentSet, null);
	}

	private ExhaustiveIterator(ArgumentSet<T> argumentSet, ExhaustiveIterator<T> prevColumn) {

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

	private Iterator<? extends T> makeIter() {
		return args.argsValues.get(argIndex).iterator();
	}

	private ExhaustiveIterator<T> makeNextColumn() {
		if (argIndex < args.argNames.size() - 1) {
			return new ExhaustiveIterator<T>(args, this);
		} else {
			return null;
		}
	}


	@Override
	protected T[] computeNext() {
		if (predicate == null)
		{
			return computeNextSimple();
		}
		return computeNextPassingPredicate();
	}


	protected T[] computeNextPassingPredicate() {
		T[] candidate = computeNextSimple();
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
	protected T[] computeNextSimple() {

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

	@SuppressWarnings("unchecked")
	private T[] populateResult() {
		Object[] ret = new Object[argIndex + 1];

		ExhaustiveIterator<T> it = this;
		for (int i = ret.length - 1; i >= 0; i--) {
			ret[i] = it.currValue;
			it = it.prevColumn;
		}

		return (T[]) ret;
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