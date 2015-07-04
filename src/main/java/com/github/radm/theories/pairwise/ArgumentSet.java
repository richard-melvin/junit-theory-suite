package com.github.radm.theories.pairwise;

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
public class ArgumentSet<T> implements Iterable<T[]> {

	static final Logger LOG = LoggerFactory.getLogger(ArgumentSet.class);
	final List<String> argNames;
	final List<List<T>> argsValues;

	private final Map<String, Predicate<Object[]>> constraints = new HashMap<>();

	/**
	 * Instantiates a new argument set.
	 *
	 * @param argNames
	 *            the arg names
	 * @param argsValues
	 *            the args values
	 */
	public ArgumentSet(List<String> argNames, List<List<T>> argsValues) {
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
	public static <T> ArgumentSet<T> fromArray(List<String> argNames, List<T[]> argsValues) {

		List <List<T>> vals = new ArrayList<>(argsValues.size());

		for (T[] o : argsValues)
		{
			vals.add(Arrays.asList(o));
		}

		return new ArgumentSet<>(argNames, vals);
	}


	/**
	 * Adds the constraint.
	 *
	 * @param argName
	 *            the arg name
	 * @param constraint
	 *            the constraint
	 */
	public ArgumentSet<T> withConstraint(String argName, Predicate<Object[]> constraint) {
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
	public Iterator<T[]> iterator() {
		return new ExhaustiveIterator<T>(this);
	}

	/**
	 * Iterate over all pairwise combinations of argument values
	 */
	public Iterator<T[]> pairwiseIterator() {

		// no point doing pairwise logic for small cases
		if (argNames.size() <= 2) {
			return iterator();
		}

		return new PairwiseIterator<T>(this);
	}


	/**
	 * Gets the arg names in declaration order.
	 *
	 * @return the arg names
	 */
	public List<String> getArgNames() {
		return argNames;
	}

}
