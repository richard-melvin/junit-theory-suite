package radm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.experimental.theories.PotentialAssignment;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.internal.Assignments;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

/**
 * Simple wrapper to allow reuse of validation logic.
 */
public class TheoriesWrapper extends Theories {

	private List<MethodWithArguments> completeAssignments = new ArrayList<>();

	public TheoriesWrapper(Class<?> klass) throws InitializationError {
		super(klass);
	}

	@Override
	protected List<FrameworkMethod> computeTestMethods() {
		return super.computeTestMethods();
	}

	public Collection<? extends FrameworkMethod> computeTestMethodsWithArgs(
			FrameworkMethod fm) {

		Assignments allUnassigned = Assignments.allUnassigned(fm.getMethod(),
				getTestClass());

		try {
			expand(fm, allUnassigned);
		} catch (Throwable e) {
			Assert.fail("Failure while collecting arguments to " + fm.getName()
					+ ":" + e.toString());
		}

		return completeAssignments;
	}

	private void expand(FrameworkMethod fm, Assignments assignments)
			throws Throwable {

		if (assignments.isComplete()) {
			completeAssignments.add(new MethodWithArguments(fm.getMethod(),
					assignments.getAllArguments()));
			return;
		}

		for (PotentialAssignment source : assignments
				.potentialsForNextUnassigned()) {
			expand(fm, assignments.assignNext(source));
		}
	}

}
