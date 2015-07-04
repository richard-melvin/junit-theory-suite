package com.github.radm.theories.pairwise;

import java.util.Iterator;

/**
 * Iterator over the contents of an argset.
 *
 * @param <T> the generic type
 */
public abstract class ArgSetIterator<T> implements Iterator<T[]> {

	protected final ArgumentSet<T> args;

	protected boolean knownComplete = false;
	protected T[] nextValue = null;

	protected abstract T[] computeNext();

	protected ArgSetIterator(ArgumentSet<T> args) {
		this.args = args;

	}

	@Override
	public boolean hasNext() {
		if (knownComplete) {
			return false;
		}
		if (nextValue != null) {
			return true;
		}

		nextValue = computeNext();


		return !knownComplete;
	}

	@Override
	public T[] next() {
		assert nextValue != null;
		T[] ret = nextValue;
		nextValue = null;
		return ret;
	}

}