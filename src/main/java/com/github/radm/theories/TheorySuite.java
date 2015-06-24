package com.github.radm.theories;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.contrib.theories.Theory;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
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

	private Map<FrameworkMethod, Description> descriptions;

	/**
	 * currently reuses some of the implementation of the default theories
	 * runner.
	 */
	private TheoriesWrapper embeddedRunner;

	private List<FrameworkMethod> allMethodsWithAllArgs;

	private List<Throwable> initFail;

	private Description suiteDescription;

	private Map<Method, AssumptionsFailureCounter> checksByMethod;

	/**
	 * Instantiates a new theory suite.
	 *
	 * @param testClass
	 *            the test class
	 * @throws InitializationError
	 *             if illegal annotations found
	 */
	public TheorySuite(Class<?> testClass) throws InitializationError {
		super(testClass);

		LOG.debug("Constructor init complete");
		if (initFail != null) {
			throw new InitializationError(initFail);
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
			return super.computeTestMethods();
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

	@Override
	protected void runChild(final FrameworkMethod fm, RunNotifier notifier) {

		if (checksByMethod.containsKey(fm.getMethod())) {
			AssumptionsFailureCounter listener = checksByMethod.get(fm
					.getMethod());

			notifier.addListener(listener);
			try {
				super.runChild(fm, notifier);
			} finally {
				if (!listener.isWithinLimit()) {
					MethodWithArguments mwa = (MethodWithArguments) fm;
					notifier.fireTestFailure(new Failure(
							describeChild(mwa.getParent()),
							new AssertionError(
									"Never found parameters that satisfied method assumptions.")));
				}
				notifier.removeListener(listener);
			}
		} else {
			super.runChild(fm, notifier);
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
	 * Initialise all data members. Needed as a lot of work gets done before
	 * constructor completes.
	 */
	private void init() {
		suiteDescription = Description.createSuiteDescription(getTestClass()
				.getJavaClass());
		allMethodsWithAllArgs = new ArrayList<>();
		descriptions = new ConcurrentHashMap<>();
		checksByMethod = new ConcurrentHashMap<>();
	}

	/**
	 * record everything for a simple test.
	 *
	 * @param fm
	 *            the framework method
	 */
	private void recordNonTheoryCase(FrameworkMethod fm) {
		LOG.debug("non-theory test {}", fm);

		allMethodsWithAllArgs.add(fm);
		Description desc = Description.createTestDescription(
				suiteDescription.getTestClass(), fm.getName());
		suiteDescription.addChild(desc);

		descriptions.put(fm, desc);

	}

	/**
	 * Record theory case.
	 *
	 * @param runner
	 *            the runner
	 * @param fm
	 *            the fm
	 */
	private void recordTheoryCase(TheoriesWrapper runner, FrameworkMethod fm) {

		Description methodDescription = Description.createSuiteDescription(fm
				.getName());
		descriptions.put(fm, methodDescription);

		suiteDescription.addChild(methodDescription);

		Collection<MethodWithArguments> methodCases = new ArgumentGenerator(
				getTestClass(), fm).computeTestMethodsWithArgs();
		if (methodCases.isEmpty()) {
			reportError(new Error("No test cases found for " + fm
					+ "; missing annotations?"));
		} else {
			recordCases(methodDescription, methodCases);

			checksByMethod.put(fm.getMethod(), new AssumptionsFailureCounter(
					methodCases.size()));
			LOG.debug("theory {} has {} cases", fm, methodCases.size());

		}

	}

	private void recordCases(Description methodDescription,
			Collection<MethodWithArguments> methodCases) {
		allMethodsWithAllArgs.addAll(methodCases);

		for (MethodWithArguments testCase : methodCases) {
			Description testDescription = Description.createTestDescription(
					suiteDescription.getTestClass(), testCase.getName());

			methodDescription.addChild(testDescription);
			descriptions.put(testCase, testDescription);
		}

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
				initFail = e.getCauses();
			}
		}

		return embeddedRunner;
	}

	private void reportError(Throwable t) {
		LOG.debug(t.toString());

		if (initFail == null) {
			initFail = new ArrayList<>();
		}

		initFail.add(t);
	}
}
