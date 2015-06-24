package com.github.radm.theories.internals;

import java.util.Iterator;
import java.util.List;


/**
 * Iterable representation of the set of lists of arguments to a test function
 *
 */
public class ArgumentSet implements Iterable<Object[]> {

	private final List<String> argNames;
	private final List<Iterable<Object>> argsValues;

	public ArgumentSet(List<String> argNames,
			List<Iterable<Object>> argsValues) {
		super();
		this.argNames = argNames;
		this.argsValues = argsValues;

		assert argNames.size() == argsValues.size();
	}

	/**
	 * Iterate over all possible values of combinations or argument values
	 */
	@Override
	public Iterator<Object[]> iterator() {
		return new ArgumentSetIterator();
	}

	/**
	 * Iterate over the contents of an argset.
	 */
	public class ArgumentSetIterator implements Iterator<Object[]>
	{

		private final int argIndex;
		private Iterator<Object> valIter;
		private final ArgumentSetIterator nextColumn;
		private final ArgumentSetIterator prevColumn;

		Object currValue;
		boolean knownComplete = false;
		Object[] nextValue;

		ArgumentSetIterator ()
		{
			argIndex = 0;
			prevColumn = null;
			nextColumn = makeNextColumn();
			valIter = makeIter();
		}

		private ArgumentSetIterator(ArgumentSetIterator prevColumn) {
			this.prevColumn = prevColumn;
			argIndex = prevColumn.argIndex + 1;

			nextColumn = makeNextColumn();
			valIter = makeIter();

		}

		private Iterator<Object> makeIter() {
			return argsValues.get(argIndex).iterator();
		}

		private ArgumentSetIterator makeNextColumn() {
			if (argIndex < argNames.size() - 1)
			{
				return new ArgumentSetIterator(this);
			}
			else
			{
				return null;
			}
		}

		@Override
		public boolean hasNext() {
			if (knownComplete)
			{
				return false;
			}
			nextValue = computeNext();

			return !knownComplete;
		}

		@Override
		public Object[] next() {
			assert nextValue != null;
			return nextValue;
		}

		/**
		 * copying a trick from Guava, combine next and hasNext into a single function
		 * with extra intermediate storage & a flag.
		 * @return next object, or null.
		 */
		private Object[] computeNext() {

			if (nextColumn == null)
			{
				if (valIter.hasNext())
				{
					currValue = valIter.next();
					return populateResult();
				}
				knownComplete = true;
				return null;
			}

			if (currValue == null && valIter.hasNext())
			{
				currValue = valIter.next();
			}

			if (nextColumn.hasNext())
			{
				return nextColumn.next();
			}
			if (valIter.hasNext())
			{
				nextColumn.reset();
				currValue = valIter.next();
				if (nextColumn.hasNext())
				{
					return nextColumn.next();
				}
			}

			knownComplete = true;
			return null;

		}

		private Object[] populateResult() {
			Object[] ret = new Object[argIndex + 1];

			ArgumentSetIterator it = this;
			for (int i = ret.length - 1; i >= 0; i--)
			{
				ret[i] = it.currValue;
				it = it.prevColumn;
			}

			return ret;
		}


		private void reset()
		{
			valIter = makeIter();
			knownComplete = false;
			currValue = null;
			if (nextColumn != null)
			{
				nextColumn.reset();
			}
		}
	}


}
