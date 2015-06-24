package com.github.radm.theories;

import java.util.List;

import org.junit.contrib.theories.Theories;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

/**
 * Simple wrapper to allow reuse of validation logic.
 */
class TheoriesWrapper extends Theories {

	public TheoriesWrapper(Class<?> klass) throws InitializationError {
		super(klass);
	}

	@Override
	public List<FrameworkMethod> computeTestMethods() {
		return super.computeTestMethods();
	}


}
