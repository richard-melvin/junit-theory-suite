package com.github.radm.theories.pairwise;

import java.util.Iterator;

/**
 * Iterator over the contents of an argset.
 *
 * @param <T> the generic type
 */
public abstract class ArgSetIterator implements Iterator<Object[]> {

	protected final ArgumentSet args;

	protected boolean knownComplete = false;
	protected Object[] nextValue = null;

	protected abstract Object[] computeNext();

	protected ArgSetIterator(ArgumentSet args) {
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
	public Object[] next() {
		assert nextValue != null;
		Object[] ret = nextValue;
		nextValue = null;
		return ret;
	}

}