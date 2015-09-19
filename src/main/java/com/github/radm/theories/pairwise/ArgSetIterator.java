package com.github.radm.theories.pairwise;

import java.util.Iterator;

/**
 * Iterator over the contents of an argset.
 *
 */
public abstract class ArgSetIterator implements Iterator<ArgVector> {

    protected final ArgumentSet args;

    protected boolean knownComplete = false;
    protected ArgVector nextValue = null;

    protected abstract ArgVector computeNext();

    protected ArgSetIterator(ArgumentSet args) {
        this.args = args;

    }

    @Override
    public boolean hasNext() {
        if (knownComplete) {
            return false;
        }
        if (nextValue != null) {
            return true;
        }

        nextValue = computeNext();

        return !knownComplete;
    }

    @Override
    public ArgVector next() {
        assert nextValue != null;
        ArgVector ret = nextValue;
        nextValue = null;
        return ret;
    }

}
