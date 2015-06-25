package com.github.radm.theories.internals;

import java.lang.reflect.Type;
import java.util.List;

import org.junit.contrib.theories.ParameterSignature;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.radm.theories.Constraint;

/**
 * Find the set of constraint functions. TODO: check for illegal constraint
 * annotations
 */
public class ConstraintFinder {

	private static final Logger LOG = LoggerFactory.getLogger(ConstraintFinder.class);

	private final TestClass testClass;
	private List<FrameworkMethod> constraintMethods;

	public ConstraintFinder(TestClass testClass) {
		super();
		this.testClass = testClass;

		constraintMethods = testClass.getAnnotatedMethods(Constraint.class);
	}

	public void applyConstraintsTo(List<ParameterSignature> signature, ArgumentSet as) {

		constraintMethods.stream().filter(cm -> matches(cm, signature)).forEach(cm -> applyTo(cm, as));

	}

	private void applyTo(FrameworkMethod fcm, ArgumentSet as) {

		int applyCheckAt = fcm.getMethod().getParameterCount() - 1;

		as.withConstraint(as.getArgNames().get(applyCheckAt), args -> checkConstraintOn(fcm, args));

	}

	private boolean checkConstraintOn(FrameworkMethod fcm, Object[] args) {

		try {
			return (boolean) fcm.invokeExplosively(testClass, args);
		} catch (Throwable e) {
			return false;
		}
	}

	// TODO: drop restriction that arguments must be in same order
	private boolean matches(FrameworkMethod fcm, List<ParameterSignature> signature) {

		int parameterCount = fcm.getMethod().getParameterCount();
		if (parameterCount > signature.size()) {
			return false;
		}

		for (int i = 0; i < parameterCount; i++) {

			Type parameterizedType = fcm.getMethod().getParameters()[i].getParameterizedType();
			if (!signature.get(i).canAcceptType(parameterizedType)) {

				LOG.info("discarding potential constraint {} as arg {} type {} wrong", fcm, i, parameterizedType);

				return false;
			}
		}
		LOG.info("using constraint {} with {} arguments", fcm, parameterCount);

		return true;
	}

}
