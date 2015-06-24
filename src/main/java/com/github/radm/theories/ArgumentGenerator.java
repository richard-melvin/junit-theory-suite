package com.github.radm.theories;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.contrib.theories.PotentialAssignment;
import org.junit.contrib.theories.internal.Assignments;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Calculate the set of arguments to use for a particular method call.
 */
public class ArgumentGenerator {

	private static final Logger LOG = LoggerFactory
			.getLogger(ArgumentGenerator.class);

	private final TestClass testClass;

	private final FrameworkMethod testMethod;

	private final List<MethodWithArguments> testsCalls = new ArrayList<>();


	/**
	 * Instantiates a new argument generator.
	 *
	 * @param testClass the test class
	 * @param testMethod the test method
	 */
	public ArgumentGenerator(TestClass testClass, FrameworkMethod testMethod) {
		super();
		this.testClass = testClass;
		this.testMethod = testMethod;
	}

	/**
	 * Compute the set of methods with known argument values.
	 *
	 * @param fm
	 *            the framework method
	 * @return the collection
	 */
	public Collection<MethodWithArguments> computeTestMethodsWithArgs() {


		try {
			Assignments allUnassigned = Assignments.allUnassigned(testMethod.getMethod(),
					testClass);

			expand(testMethod, allUnassigned);
		} catch (Throwable e) {
			LOG.warn("collecting arguments", e);
			Assert.fail("Failure while collecting arguments to " + testMethod.getName()
					+ ":" + e.toString());
		}

		return testsCalls;
	}

	private void expand(FrameworkMethod fm, Assignments assignments)
			throws Throwable {

		if (assignments.isComplete()) {
			MethodWithArguments testCall = new MethodWithArguments(
					fm.getMethod(), assignments.getMethodArguments());

			LOG.trace("Identified test case {}", testCall);

			testsCalls.add(testCall);
			return;
		}

		for (PotentialAssignment source : assignments
				.potentialsForNextUnassigned()) {
			expand(fm, assignments.assignNext(source));
		}
	}

}
