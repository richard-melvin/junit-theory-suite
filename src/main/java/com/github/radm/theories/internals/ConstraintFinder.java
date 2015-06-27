package com.github.radm.theories.internals;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import com.github.radm.theories.Constraint;

/**
 * Find the set of constraint functions. TODO: check for illegal constraint
 * annotations
 */
public class ConstraintFinder {


	private final TestClass testClass;
	private final List<MethodSignature> constraintMethods;

	/**
	 * Instantiates a new constraint finder.
	 *
	 * @param testClass the test class
	 */
	public ConstraintFinder(TestClass testClass) {
		super();
		this.testClass = testClass;

		List<FrameworkMethod> annotatedMethods = testClass.getAnnotatedMethods(Constraint.class);

		constraintMethods = annotatedMethods.stream().map(MethodSignature::new).collect(Collectors.toList());

	}

	/**
	 * Apply matching constraints defined on current class to the set of test method arguments.
	 *
	 * @param fm the fm
	 * @param as the as
	 */
	public void applyConstraintsTo(FrameworkMethod fm, ArgumentSet<Object> as) {

		MethodSignature testSignature = new MethodSignature(fm);

		constraintMethods.stream().filter(cm -> cm.isSubListOf(testSignature))
				.forEach(cm -> applyTo(cm, testSignature, as));

	}

	private void applyTo(MethodSignature constraint, MethodSignature testSignature, ArgumentSet<Object> as) {

		for (MethodSignature.Shim argMapping : constraint.buildShims(testSignature)) {

			as.withConstraint(as.getArgNames().get(argMapping.lastMappedArgIndex()),
					args -> checkConstraintOn(constraint.getFrameworkMethod(), argMapping, args));
		}


	}

	private boolean checkConstraintOn(FrameworkMethod fcm, MethodSignature.Shim argMapping, Object[] args) {

		try {
			return (boolean) fcm.invokeExplosively(testClass, argMapping.apply(args));
		} catch (Throwable e) {
			return false;
		}
	}

}
