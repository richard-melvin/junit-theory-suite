package com.github.radm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.experimental.theories.PotentialAssignment;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.internal.Assignments;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple wrapper to allow reuse of validation logic.
 */
public class TheoriesWrapper extends Theories {

	private static final Logger LOG = LoggerFactory
			.getLogger(TheoriesWrapper.class);

	private List<MethodWithArguments> testsCalls = new ArrayList<>();

	public TheoriesWrapper(Class<?> klass) throws InitializationError {
		super(klass);
	}

	@Override
	protected List<FrameworkMethod> computeTestMethods() {
		return super.computeTestMethods();
	}

	/**
	 * Compute the set of methods with known argument values.
	 *
	 * @param fm
	 *            the framework method
	 * @return the collection
	 */
	public Collection<MethodWithArguments> computeTestMethodsWithArgs(
			FrameworkMethod fm) {

		Assignments allUnassigned = Assignments.allUnassigned(fm.getMethod(),
				getTestClass());

		testsCalls.clear();

		try {
			expand(fm, allUnassigned);
		} catch (Throwable e) {
			Assert.fail("Failure while collecting arguments to " + fm.getName()
					+ ":" + e.toString());
		}

		return testsCalls;
	}

	private void expand(FrameworkMethod fm, Assignments assignments)
			throws Throwable {

		if (assignments.isComplete()) {
			MethodWithArguments testCall = new MethodWithArguments(
					fm.getMethod(), assignments.getAllArguments());

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
