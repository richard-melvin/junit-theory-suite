package radm;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.StringJoiner;

import org.junit.runners.model.FrameworkMethod;

/**
 * A test method with an expanded-out set of arguments ready to be run.
 */
public class MethodWithArguments extends FrameworkMethod {

	final Object[] args;

	public MethodWithArguments(Method method, Object[] args) {
		super(method);
		this.args = args;
	}

	@Override
	public Object invokeExplosively(Object target, Object... params)
			throws Throwable {
		return super.invokeExplosively(target, args);
	}

	@Override
	public String getName() {

		StringJoiner sj = new StringJoiner(",", super.getName() + "(", ")");

		for (int i = 0; i < args.length; i++) {
			sj.add(args[i].toString());
		}

		return sj.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			return false;
		}

		if (!(obj instanceof MethodWithArguments)) {
			return false;
		}
		MethodWithArguments other = (MethodWithArguments) obj;

		return Arrays.equals(args, other.args);
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}

}
