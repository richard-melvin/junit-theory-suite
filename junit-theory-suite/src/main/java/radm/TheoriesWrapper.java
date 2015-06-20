package radm;

import java.util.List;

import org.junit.experimental.theories.Theories;
import org.junit.runners.model.InitializationError;

/**
 * Simple wrapper to allow reuse of validation logic.
 */
public class TheoriesWrapper extends Theories {

	public TheoriesWrapper(Class<?> klass) throws InitializationError {
		super(klass);
	}

	@Override
	public void collectInitializationErrors(List<Throwable> errors) {
		super.collectInitializationErrors(errors);
	}
}
