package com.github.radm.theories.pairwise;

import java.util.Arrays;

/**
 * A single specific set of argument values. Allows convenient conversion
 * between index and value form.
 */
public class ArgVector {

	private final ArgumentSet argSet;
	public final int[] args;
	private Object[] argVals;

	public ArgVector(ArgumentSet argSet) {
		super();
		this.argSet = argSet;
		args = new int[argSet.argNames.size()];
		Arrays.setAll(args, i -> -1);

	}

	private ArgVector(ArgumentSet argSet, int[] args) {
		super();
		this.argSet = argSet;
		this.args = args.clone();
	}

	/**
	 * number of arguments.
	 *
	 * @return the size
	 */
	public int size() {
		return args.length;
	}

	/**
	 * Gets the actual argument values.
	 *
	 * @return the arg vals
	 */
	public Object[] getArgVals() {
		if (argVals == null) {
			argVals = fillIn();
		}
		return argVals;
	}

	/**
	 * Return a copy of vector with specified column set.
	 *
	 * @param extraValue
	 *            the extra value
	 * @param col
	 *            the col
	 * @return the arg vector
	 */
	public ArgVector withValue(int extraValue, int col) {

		ArgVector withValue = new ArgVector(argSet, args);

		withValue.args[col] = extraValue;
		return withValue;
	}

	private Object[] fillIn() {

		Object[] ret = new Object[args.length];

		for (int i = 0; i < args.length; i++) {
			if (args[i] >= 0) {
				ret[i] = argSet.argsValues.get(i).get(args[i]);
			}
		}
		return ret;
	}

	@Override
	public String toString() {
		return Arrays.toString(args);
	}

}
