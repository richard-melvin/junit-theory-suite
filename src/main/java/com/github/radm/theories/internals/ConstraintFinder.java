package com.github.radm.theories.internals;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.junit.contrib.theories.Theory;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.radm.theories.Constraint;
import com.github.radm.theories.WithConstraints;

/**
 * Find the set of constraint functions.
 */
public class ConstraintFinder {

	private static final Logger LOG = LoggerFactory.getLogger(ConstraintFinder.class);

	private final List<MethodSignature> globalConstraints = new ArrayList<>();
	private final Map<String, List<MethodSignature>> namedConstraints = new HashMap<>();

	/**
	 * Instantiates a new constraint finder.
	 *
	 * @param testClass
	 *            the test class
	 */
	public ConstraintFinder(TestClass testClass, Consumer<Error> handler) {
		super();
		List<FrameworkMethod> annotatedMethods = new ArrayList<>(testClass.getAnnotatedMethods(Constraint.class));
		annotatedMethods.removeIf(am -> checkValidity(am.getMethod(), handler));

		populateConstraints(annotatedMethods);

		List<FrameworkMethod> constrainedTheories = testClass.getAnnotatedMethods(WithConstraints.class);

		constrainedTheories.forEach(fm -> checkExplicitConstraintUsage(fm, handler));

	}

	private void populateConstraints(List<FrameworkMethod> annotatedMethods) {
		for (FrameworkMethod fm : annotatedMethods) {
			MethodSignature ms = new MethodSignature(fm);
			String name = fm.getMethod().getAnnotation(Constraint.class).value();
			if (name.length() == 0) {
				globalConstraints.add(ms);
			} else {

				if (!namedConstraints.containsKey(name)) {
					namedConstraints.put(name, new ArrayList<>());
				}
				namedConstraints.get(name).add(ms);
			}
		}
	}

	private boolean checkValidity(Method method, Consumer<Error> handler) {

		boolean invalid = false;
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

	private void checkExplicitConstraintUsage(FrameworkMethod fm, Consumer<Error> handler) {

		Method method = fm.getMethod();

		if (!method.isAnnotationPresent(Theory.class)) {
			handler.accept(new Error("WithConstraint used on a non-theory " + method.getName()));
		}

		String[] names = method.getAnnotation(WithConstraints.class).value();

		if (names.length == 0) {
			handler.accept(new Error("WithConstraint used without any constraint names " + method.getName()));
		}

		for (String name : names) {

			if (!namedConstraints.containsKey(name)) {
				handler.accept(new Error(
						"WithConstraint used with unknown constraint name" + name + " in " + method.getName()));
			}

			MethodSignature testSignature = new MethodSignature(fm);

			boolean matchFound = false;
			for (MethodSignature namedConstraint : namedConstraints.get(name)) {
				if (namedConstraint.isSubListOf(testSignature)) {
					matchFound = true;
				}
			}
			if (!matchFound) {
				handler.accept(new Error("WithConstraint used with no matching constraint names "
						+ name + " in " + method.getName()));

			}

		}

	}

	/**
	 * Apply matching constraints defined on current class to the set of test
	 * method arguments.
	 *
	 * @param fm
	 *            the fm
	 * @param as
	 *            the as
	 */
	public void applyConstraintsTo(FrameworkMethod fm, ArgumentSet<Object> as) {

		MethodSignature testSignature = new MethodSignature(fm);

		List<MethodSignature> applicableConstraints = new ArrayList<>(globalConstraints);

		if (fm.getMethod().isAnnotationPresent(WithConstraints.class)) {
			for (String name : fm.getMethod().getAnnotation(WithConstraints.class).value()) {
				applicableConstraints.addAll(namedConstraints.get(name));
			}
		}

		applicableConstraints.stream().filter(cm -> cm.isSubListOf(testSignature))
				.forEach(cm -> applyTo(cm, testSignature, as));

	}

	private void applyTo(MethodSignature constraint, MethodSignature testSignature, ArgumentSet<Object> as) {

		for (MethodSignature.Shim argMapping : constraint.buildShims(testSignature)) {

			String argName = as.getArgNames().get(argMapping.lastMappedArgIndex());
			if (LOG.isDebugEnabled()) {
				LOG.debug("Add constraint {} @ {}", constraint.getFrameworkMethod(), argName);
			}
			as.withConstraint(argName, args -> checkConstraintOn(constraint.getFrameworkMethod(), argMapping, args));
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
