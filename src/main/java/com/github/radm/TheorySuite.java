package com.github.radm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.experimental.theories.Theory;
import org.junit.runner.Description;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A TheorySuite is a JUnit test runner that understands all the test
 * annotations supported by the standard JUnit `Theories` runner. Unlike the
 * standard runner, it treats each combination of parameters as a distinct test
 * case.
 */
public class TheorySuite extends BlockJUnit4ClassRunner {

	private static final Logger LOG = LoggerFactory
			.getLogger(TheorySuite.class);

	private ConcurrentHashMap<FrameworkMethod, Description> descriptions = new ConcurrentHashMap<>();

	/**
	 * currently reuses some of the implementation of the default theories
	 * runner.
	 */
	private TheoriesWrapper embeddedRunner;

	private List<FrameworkMethod> allMethodsWithAllArgs = null;

	private InitializationError initFail = null;

	private Description suiteDescription;

	public TheorySuite(Class<?> testClass) throws InitializationError {
		super(testClass);

		if (initFail != null) {
			throw initFail;
		}

	}

	@Override
	public Description getDescription() {
		assert suiteDescription != null;
		return suiteDescription;
	}

	@Override
	protected List<FrameworkMethod> computeTestMethods() {

		TheoriesWrapper runner = getEmbeddedRunner();

		if (runner == null) {
			return Collections.emptyList();
		}

		if (allMethodsWithAllArgs == null) {
			init();
			for (FrameworkMethod fm : runner.computeTestMethods()) {
				if (fm.getAnnotation(Theory.class) == null) {
					recordNonTheoryCase(fm);
				} else {
					recordTheoryCase(runner, fm);
				}
			}
		}

		return allMethodsWithAllArgs;
	}

	/**
	 * Initialise all data members. Needed as a lot of work gets done before
	 * constructor completes.
	 */
	private void init() {
		suiteDescription = Description.createSuiteDescription(getTestClass().getJavaClass());
		allMethodsWithAllArgs = new ArrayList<>();
		descriptions = new ConcurrentHashMap<>();
	}

	private void recordNonTheoryCase(FrameworkMethod fm) {
		if (LOG.isDebugEnabled())
		{
			LOG.debug("non-theory test {}", fm);
		}
		allMethodsWithAllArgs.add(fm);
		Description desc = Description.createTestDescription(
				suiteDescription.getTestClass(), fm.getName());
		suiteDescription.addChild(desc);

		descriptions.put(fm, desc);

	}

	private void recordTheoryCase(TheoriesWrapper runner, FrameworkMethod fm) {
		if (LOG.isDebugEnabled())
		{
			LOG.debug("theory {}", fm);
		}

		Description methodDescription = Description.createSuiteDescription(fm
				.getName());
		suiteDescription.addChild(methodDescription);
		Collection<MethodWithArguments> methodCases = runner
				.computeTestMethodsWithArgs(fm);
		recordCases(methodDescription, methodCases);

	}

	private void recordCases(Description methodDescription,
			Collection<MethodWithArguments> methodCases) {
		allMethodsWithAllArgs.addAll(methodCases);

		for (MethodWithArguments testCase : methodCases) {
			Description testDescription = Description.createTestDescription(suiteDescription.getTestClass(),
					testCase.getName());

			methodDescription.addChild(testDescription);
			descriptions.put(testCase, testDescription);
		}

	}

	@Override
	public int testCount() {
		return computeTestMethods().size();
	}

	@Override
	protected Description describeChild(FrameworkMethod method) {

		assert descriptions.containsKey(method);

		return descriptions.get(method);

	}

	/**
	 * Gets the embedded runner; needed as virtual methods get called from super
	 * constructor
	 *
	 * @return the embedded runner
	 */
	private TheoriesWrapper getEmbeddedRunner() {
		if (embeddedRunner == null) {
			try {
				embeddedRunner = new TheoriesWrapper(getTestClass()
						.getJavaClass());
			} catch (InitializationError e) {
				initFail = e;
			}
		}

		return embeddedRunner;
	}

}
