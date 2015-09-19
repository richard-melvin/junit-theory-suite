package com.github.radm.theories.runner;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

import org.junit.runners.model.FrameworkMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A test method with an expanded-out set of arguments ready to be run.
 */
public class MethodWithArguments extends FrameworkMethod {

    private static final Logger LOG = LoggerFactory.getLogger(MethodWithArguments.class);

    private final Object[] args;

    /**
     * Instantiates a new method with arguments.
     *
     * @param method
     *            the method
     * @param args
     *            the args
     */
    public MethodWithArguments(Method method, Object[] args) {
        super(method);
        this.args = args;
    }

    @Override
    public Object invokeExplosively(Object target, Object... params) throws Throwable {

        LOG.trace("Executing {}", this);

        return super.invokeExplosively(target, args);
    }

    @Override
    public String getName() {

        StringJoiner sj = new StringJoiner(",", super.getName() + "[", "]");

        for (int i = 0; i < args.length; i++) {
            sj.add(Objects.toString(args[i]));
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
        return Objects.hash(getMethod(), Arrays.hashCode(args));
    }

    @Override
    public String toString() {
        return getName();
    }

    /**
     * Gets the underlying method, without arguments specified.
     *
     * @return the parent
     */
    public FrameworkMethod getParent() {
        return new FrameworkMethod(getMethod());
    }
}
