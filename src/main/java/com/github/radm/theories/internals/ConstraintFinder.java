package com.github.radm.theories.internals;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.radm.theories.Constraint;

/**
 * Find the set of constraint functions.
 */
public class ConstraintFinder {

	private static final Logger LOG = LoggerFactory
			.getLogger(ConstraintFinder.class);

	private final List<MethodSignature> constraintMethods;

	/**
	 * Instantiates a new constraint finder.
	 *
	 * @param testClass the test class
	 */
	public ConstraintFinder(TestClass testClass, Consumer<Error> handler ) {
		super();
		List<FrameworkMethod> annotatedMethods = testClass.getAnnotatedMethods(Constraint.class);

		constraintMethods = annotatedMethods.stream().map(MethodSignature::new).collect(Collectors.toList());

		constraintMethods.removeIf(ms -> checkValidity(ms.getFrameworkMethod().getMethod(), handler));

	}

	private boolean checkValidity(Method method, Consumer<Error> handler) {

		boolean invalid = false;;
        if (!Modifier.isStatic(method.getModifiers())) {
        	handler.accept(new Error("Constraint method " + method.getName() + " must be static"));
        	invalid = true;
        }
        if (!Modifier.isPublic(method.getModifiers())) {
        	handler.accept(new Error("Constraint method " + method.getName() + " must be public"));
        	invalid = true;
        }

        if (method.getParameterCount() == 0) {
        	handler.accept(new Error("Constraint method " + method.getName() + " must have at least one argument"));
        	invalid = true;
        }

        if (!method.getReturnType().isAssignableFrom(Boolean.TYPE)) {
        	handler.accept(new Error("Constraint method " + method.getName() + " must return boolean value"));
        	invalid = true;
        }

        return invalid;
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

			String argName = as.getArgNames().get(argMapping.lastMappedArgIndex());
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Add constraint {} @ {}", constraint.getFrameworkMethod(), argName);
			}
			as.withConstraint(argName,
					args -> checkConstraintOn(constraint.getFrameworkMethod(), argMapping, args));
		}

	}

	private boolean checkConstraintOn(FrameworkMethod fcm, MethodSignature.Shim argMapping, Object[] args) {

		try {
			return (boolean) fcm.invokeExplosively(null, argMapping.apply(args));
		} catch (Throwable e) {
			LOG.debug("Exception while checking constraint:", e);
			return false;
		}
	}

}
