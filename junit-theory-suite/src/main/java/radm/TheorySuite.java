package radm;

import java.util.List;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

public class TheorySuite extends BlockJUnit4ClassRunner {

	private final TheoriesWrapper embeddedRunner;

	public TheorySuite(Class<?> testClass) throws InitializationError {
		super(testClass);

		embeddedRunner = new TheoriesWrapper(testClass);
	}

	@Override
	protected List<FrameworkMethod> getChildren() {
		return super.getChildren();
	}




}
