package com.github.radm.theories.internals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Iterable representation of the set of lists of arguments to a test function.
 * Constraints should be added at the point they are first able to be evaluated.
 * They should then prevent all matching sets values from being returned.
 *
 */
public class ArgumentSet implements Iterable<Object[]> {

	private static final Logger LOG = LoggerFactory.getLogger(ArgumentSet.class);
	private final List<String> argNames;
	private final List<? extends Iterable<Object>> argsValues;

	private final Map<String, Predicate<Object[]>> constraints = new HashMap<>();

	/**
	 * Instantiates a new argument set.
	 *
	 * @param argNames
	 *            the arg names
	 * @param argsValues
	 *            the args values
	 */
	public ArgumentSet(List<String> argNames, List<? extends Iterable<Object>> argsValues) {
		super();
		this.argNames = argNames;
		this.argsValues = argsValues;

		assert argNames.size() == argsValues.size();
	}


	/**
	 * create an argument set where arguments are a list of array.
	 *
	 * @param argNames the arg names
	 * @param argsValues the arg values
	 * @return the argument set
	 */
	public static ArgumentSet fromArray(List<String> argNames, List<Object[]> argsValues) {

		List <List<Object>> vals = new ArrayList<>(argsValues.size());

		for (Object[] o : argsValues)
		{
			vals.add(Arrays.asList(o));
		}

		return new ArgumentSet(argNames, vals);
	}


	/**
	 * Adds the constraint.
	 *
	 * @param argName
	 *            the arg name
	 * @param constraint
	 *            the constraint
	 */
	public ArgumentSet withConstraint(String argName, Predicate<Object[]> constraint) {
		Predicate<Object[]> existing = constraints.get(argName);
		if (existing == null) {
			constraints.put(argName, constraint);
		} else {
			constraints.put(argName, existing.and(constraint));
		}
		return this;
	}

	/**
	 * Gets the consolidated constraint that can be evaluated at this point.
	 *
	 * @param argIndex the argument index
	 * @return the constraint, or null if none
	 */
	Predicate<Object[]> getConstraint(int argIndex) {
		assert argIndex < argNames.size();
		return constraints.get(argNames.get(argIndex));

	}

	/**
	 * Iterate over all possible values of combinations or argument values
	 */
	@Override
	public Iterator<Object[]> iterator() {
		return new ArgumentSetIterator();
	}

	/**
	 * Iterate over the contents of an argset, while applying constraints.
	 */
	public class ArgumentSetIterator implements Iterator<Object[]> {

		private final int argIndex;
		private Iterator<Object> valIter;
		private final ArgumentSetIterator nextColumn;
		private final ArgumentSetIterator prevColumn;
		private final Predicate<Object[]> predicate;

		Object currValue;
		boolean knownComplete = false;
		Object[] nextValue = null;

		ArgumentSetIterator() {
			this(null);
		}

		private ArgumentSetIterator(ArgumentSetIterator prevColumn) {

			this.prevColumn = prevColumn;
			if (prevColumn == null) {
				argIndex = 0;
			} else {
				argIndex = prevColumn.argIndex + 1;
			}

			nextColumn = makeNextColumn();
			valIter = makeIter();

			predicate = getConstraint(argIndex);

		}

		private Iterator<Object> makeIter() {
			return argsValues.get(argIndex).iterator();
		}

		private ArgumentSetIterator makeNextColumn() {
			if (argIndex < argNames.size() - 1) {
				return new ArgumentSetIterator(this);
			} else {
				return null;
			}
		}

		@Override
		public boolean hasNext() {
			if (knownComplete) {
				return false;
			}
			if (nextValue != null) {
				return true;
			}
			if (predicate != null)
			{
				nextValue = computeNextPassingPredicate();
			}
			else
			{
				nextValue = computeNext();
			}

			return !knownComplete;
		}

		@Override
		public Object[] next() {
			assert nextValue != null;
			Object[] ret = nextValue;
			nextValue = null;
			return ret;
		}

		private Object[] computeNextPassingPredicate()
		{
			Object[] candidate = computeNext();
			boolean requiresReset = false;
			while (!predicate.test(populateResult()) && !knownComplete)
			{
				LOG.info("Rejected {}", Arrays.toString(populateResult()));
				if (valIter.hasNext())
				{
					currValue = valIter.next();
					LOG.info("Trying {}", Arrays.toString(populateResult()));
				}
				else
				{
					knownComplete = true;
				}
				requiresReset = true;
			}
			if (requiresReset && !knownComplete)
			{
				if (nextColumn != null)
				{
					nextColumn.reset();
					candidate = computeNext();
				}
				else
				{
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
		private Object[] computeNext() {

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

			ArgumentSetIterator it = this;
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

}
