package com.github.radm.theories.runner;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.junit.runners.model.FrameworkMethod;

/**
 * Capture the set of differently-typed arguments to a function.
 *
 */
public class MethodSignature {

    private final FrameworkMethod fm;

    /**
     * Instantiates a new method signature.
     *
     * @param method
     *            the method
     */
    public MethodSignature(FrameworkMethod method) {
        super();
        this.fm = method;

    }

    /**
     * Checks if this is a sub list of the other.
     *
     * @param other
     *            the other
     * @return true, if is sub list of
     */
    public boolean isSubListOf(MethodSignature other) {

        int maxStart = other.getMethod().getParameterCount() - getMethod().getParameterCount();

        for (int i = 0; i <= maxStart; i++) {

            if (isMatchFrom(other, i)) {
                return true;
            }
        }

        return false;
    }

    private boolean isMatchFrom(MethodSignature other, int start) {

        for (int i = 0; i < getMethod().getParameterCount(); i++) {
            if (!getMethod().getParameters()[i].getType()
                    .isAssignableFrom(other.getMethod().getParameters()[start + i].getType())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Build the set of possible argument mappings between this function and
     * another.
     *
     * @param other
     *            the other
     * @return the list
     */
    public List<Shim> buildShims(MethodSignature other) {

        List<Shim> shims = new ArrayList<>();
        int maxStart = other.getMethod().getParameterCount() - getMethod().getParameterCount();

        for (int i = 0; i <= maxStart; i++) {

            if (isMatchFrom(other, i)) {
                int[] mapping = IntStream.range(i, i + getMethod().getParameterCount()).toArray();

                shims.add(new Shim(mapping));
            }
        }

        return shims;
    }

    /**
     * Defines a mapping between the arguments of two functions, where one is a
     * subset of the other.
     */
    public static class Shim implements Function<Object[], Object[]> {

        final int[] argMapping;

        /**
         * Instantiates a new shim.
         *
         * @param argMapping the arg mapping
         */
        public Shim(int[] argMapping) {
            super();
            this.argMapping = argMapping;
        }

        @Override
        public Object[] apply(Object[] input) {

            Object[] mapped = new Object[argMapping.length];

            for (int i = 0; i < mapped.length; i++) {
                assert input.length >= argMapping[i];
                mapped[i] = input[argMapping[i]];
            }

            return mapped;
        }

        /**
         * Last argument that needs to be known to call this function.
         *
         * @return the -0based argument index
         */
        public int lastMappedArgIndex() {
            int lastMapped = -1;

            for (int arg : argMapping) {
                if (arg > lastMapped) {
                    lastMapped = arg;
                }
            }

            return lastMapped;
        }
    }

    /**
     * Gets the framework method.
     *
     * @return the framework method
     */
    public FrameworkMethod getFrameworkMethod() {
        return fm;
    }

    private Method getMethod() {
        return fm.getMethod();
    }

}
