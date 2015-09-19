
package com.github.radm.theories.runner;

import java.util.List;

import org.junit.contrib.theories.Theories;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

/**
 * Simple wrapper to allow reuse of validation logic.
 */
public class TheoriesWrapper extends Theories {

	/**
	 * Instantiates a new theories wrapper.
	 *
	 * @param klass
	 *            the klass
	 * @throws InitializationError
	 *             the initialization error
	 */
	public TheoriesWrapper(Class<?> klass) throws InitializationError {
		super(klass);
	}

	@Override
	public List<FrameworkMethod> computeTestMethods() {
		return super.computeTestMethods();
	}

}
