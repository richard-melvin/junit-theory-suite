package com.github.radm.theories;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.contrib.theories.Theory;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.radm.theories.internals.ArgumentGenerator;
import com.github.radm.theories.internals.AssumptionsFailureCounter;
import com.github.radm.theories.internals.ConstraintFinder;
import com.github.radm.theories.internals.MethodWithArguments;
import com.github.radm.theories.internals.PotentialAssignmentFinder;
import com.github.radm.theories.internals.TheoriesWrapper;

/**
 * A TheorySuite is a JUnit test runner that understands all the test
 * annotations supported by the standard JUnit `Theories` runner. Unlike the
 * standard runner, it treats each combination of parameters as a distinct test
 * case.
 */
public class TheorySuite extends BlockJUnit4ClassRunner {

	private static final Logger LOG = LoggerFactory.getLogger(TheorySuite.class);

	private Map<FrameworkMethod, Description> descriptions;

	/**
	 * currently reuses some of the implementation of the default theories
	 * runner.
	 */
	private TheoriesWrapper embeddedRunner;

	private List<FrameworkMethod> allMethodsWithAllArgs;

	private List<Throwable> initFail = null;

	private Description suiteDescription;

	private Map<Method, AssumptionsFailureCounter> checksByMethod;

	private Map<Method, MethodWithArguments> argsByMethod;

	private PotentialAssignmentFinder finder;

	private ConstraintFinder constraints;

	private Filter filter;

	private Sorter sorter;

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

	/**
	 * The defines the tree of tests that will be run.
	 */
	@Override
	public Description getDescription() {

		ensureInit();
		if (filter != Filter.ALL || sorter != null) {
			suiteDescription = rebuildDescriptionByFilter(suiteDescription);
		}

		return suiteDescription;
	}

	private Description rebuildDescriptionByFilter(Description description) {

		Description ret = description.childlessCopy();

		ArrayList<Description> children = description.getChildren();
		if (sorter != null) {
			children.sort(sorter);
		}
		for (Description child : children) {
			if (filterAppliesToAny(child, filter)) {
				ret.addChild(rebuildDescriptionByFilter(child));
			}
		}
		return ret;

	}

	private static boolean filterAppliesToAny(Description d, Filter f) {
		if (f.shouldRun(d)) {
			return true;
		}

		for (Description c : d.getChildren()) {
			if (filterAppliesToAny(c, f)) {
				return true;
			}
		}

		return false;

	}

	@Override
	protected void collectInitializationErrors(List<Throwable> errors) {

		computeTestMethods();
		if (initFail != null) {
			errors.addAll(initFail);
			initFail = null;
		}

	}

	@Override
	protected List<FrameworkMethod> computeTestMethods() {

		TheoriesWrapper runner = getEmbeddedRunner();

		if (runner == null) {
			return super.computeTestMethods();
		}

		ensureInit();
		if (allMethodsWithAllArgs == null) {
			computeTestMethodsWithArgs(runner);

		}

		return allMethodsWithAllArgs;
	}

	@Override
	public void filter(Filter filter) throws NoTestsRemainException {
		super.filter(filter);

		this.filter = filter;
	}

	@Override
	public void sort(Sorter sorter) {
		super.sort(sorter);

		this.sorter = sorter;
	}

	private void computeTestMethodsWithArgs(TheoriesWrapper runner) {
		allMethodsWithAllArgs = new ArrayList<>();

		for (FrameworkMethod fm : runner.computeTestMethods()) {

			if (fm.getAnnotation(Theory.class) == null) {
				recordNonTheoryCase(fm);
			} else {
				recordTheoryCase(runner, fm);
			}

			if (initFail != null) {
				break;
			}
		}
	}

	@Override
	protected void runChild(final FrameworkMethod fm, RunNotifier notifier) {

		if (checksByMethod.containsKey(fm.getMethod())) {
			AssumptionsFailureCounter listener = checksByMethod.get(fm.getMethod());

			notifier.addListener(listener);
			try {
				super.runChild(fm, notifier);
				if (!listener.isWithinLimit()) {
					MethodWithArguments mwa = (MethodWithArguments) fm;
					notifier.fireTestFailure(new Failure(describeChild(mwa.getParent()),
							new AssertionError("Never found parameters that satisfied method assumptions.")));
				}
			} finally {
				notifier.removeListener(listener);
			}
		} else {
			super.runChild(fm, notifier);
		}
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
	private void ensureInit() {
		if (suiteDescription == null) {

			suiteDescription = Description.createSuiteDescription(getTestClass().getJavaClass());
			descriptions = new ConcurrentHashMap<>();
			checksByMethod = new ConcurrentHashMap<>();
			argsByMethod = new ConcurrentHashMap<>();
			finder = new PotentialAssignmentFinder(getTestClass());
			constraints = new ConstraintFinder(getTestClass(), this::reportError);
			filter = Filter.ALL;
		}
	}

	/**
	 * record everything for a simple test.
	 *
	 * @param fm
	 *            the framework method
	 */
	private void recordNonTheoryCase(FrameworkMethod fm) {

		Description desc = Description.createTestDescription(suiteDescription.getTestClass(), fm.getName());
		LOG.debug("non-theory test {} ", fm);

		if (filter.shouldRun(desc)) {
			LOG.trace("passes filter as {} ", desc);

			allMethodsWithAllArgs.add(fm);
			suiteDescription.addChild(desc);

			descriptions.put(fm, desc);
		}

	}

	/**
	 * Record everything for a theory.
	 *
	 * @param runner
	 *            the runner
	 * @param fm
	 *            the framework method
	 */
	private void recordTheoryCase(TheoriesWrapper runner, FrameworkMethod fm) {

		try {
			Collection<MethodWithArguments> methodCases = new ArgumentGenerator(finder, constraints, fm)
					.computeTestMethodsWithArgs();
			Description methodDescription = Description.createSuiteDescription(fm.getName());

			if (filter.shouldRun(methodDescription)) {

				descriptions.put(fm, methodDescription);

				suiteDescription.addChild(methodDescription);
				if (methodCases.isEmpty()) {
					reportError(new Error("No test cases found for " + fm + "; missing annotations?"));
				} else {
					recordCases(fm, methodDescription, methodCases);

					checksByMethod.put(fm.getMethod(), new AssumptionsFailureCounter(methodCases.size()));
					LOG.debug("theory {} has {} cases", fm, methodCases.size());

				}
			}

		} catch (Throwable e) {
			LOG.debug("collecting arguments", e);

			reportError(e);
		}
	}

	private void recordCases(FrameworkMethod fm, Description methodDescription,
			Collection<MethodWithArguments> methodCases) {
		allMethodsWithAllArgs.addAll(methodCases);

		for (MethodWithArguments testCase : methodCases) {
			Description testDescription = Description.createTestDescription(getTestClass().getJavaClass(),
					testCase.getName());

			methodDescription.addChild(testDescription);
			descriptions.put(testCase, testDescription);
			argsByMethod.put(fm.getMethod(), testCase);
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
				embeddedRunner = new TheoriesWrapper(getTestClass().getJavaClass());
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
