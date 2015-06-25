package com.github.radm.theories.internals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.contrib.theories.ParameterSignature;
import org.junit.contrib.theories.PotentialAssignment;
import org.junit.runners.model.FrameworkMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Calculate the set of arguments to use for a particular method call.
 */
public class ArgumentGenerator {

	private static final Logger LOG = LoggerFactory
			.getLogger(ArgumentGenerator.class);

	private final PotentialAssignmentFinder finder;

	private final FrameworkMethod testMethod;

	private final List<MethodWithArguments> testsCalls = new ArrayList<>();

	private final ConstraintFinder constraints;

	/**
	 * Instantiates a new argument generator.
	 *
	 * @param testClass
	 *            the test class
	 * @param testMethod
	 *            the test method
	 */
	public ArgumentGenerator(PotentialAssignmentFinder finder,
			ConstraintFinder constraints,
			FrameworkMethod testMethod) {
		super();
		this.finder = finder;
		this.testMethod = testMethod;
		this.constraints = constraints;
	}

	/**
	 * Compute the set of methods with known argument values.
	 *
	 * @param fm
	 *            the framework method
	 * @return the collection
	 * @throws Throwable if something goes wrong with test code calculating arguments
	 */
	public Collection<MethodWithArguments> computeTestMethodsWithArgs() throws Throwable {

		List<ParameterSignature> signatures = ParameterSignature
				.signatures(testMethod.getMethod());
		List<String> colNames = new ArrayList<>(signatures.stream()
				.map(ParameterSignature::getName).collect(Collectors.toList()));

		List<Iterable<Object>> allArgValues = new ArrayList<>(signatures.size());
		for (ParameterSignature sig : signatures) {
			List<PotentialAssignment> potentialsFor = finder.potentialsFor(sig);
			List<Object> argVal = new ArrayList<>(potentialsFor.size());

			for (PotentialAssignment pa : potentialsFor) {
				argVal.add(pa.getValue());
			}
			allArgValues.add(argVal);
		}

		ArgumentSet as = new ArgumentSet(colNames, allArgValues);
		constraints.applyConstraintsTo(signatures, as);

		for (Object[] rawArgs : as) {

			assert rawArgs.length == testMethod.getMethod().getParameterCount();
			MethodWithArguments testCall = new MethodWithArguments(
					testMethod.getMethod(), rawArgs);

			LOG.trace("Identified test case {}", testCall);

			testsCalls.add(testCall);
		}

		return testsCalls;
	}

}
