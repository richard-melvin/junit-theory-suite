package com.github.radm.theories;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * Counter of assumption failures.
 *
 */
class AssumptionsFailureCounter extends RunListener {


	private AtomicInteger assumptionsFailed = new AtomicInteger();
	private final int assumptionLimit;

	@Override
	public void testAssumptionFailure(Failure failure) {
		super.testAssumptionFailure(failure);
		assumptionsFailed.incrementAndGet();
	}

	/**
	 * Instantiates a new assumptions failure counter.
	 *
	 * @param assumptionLimit the assumption limit
	 */
	public AssumptionsFailureCounter(int assumptionLimit) {
		this.assumptionLimit = assumptionLimit;
	}

	/**
	 * Checks if numer of failed assumptions is within the limit.
	 *
	 * @return true, if so
	 */
	public boolean isWithinLimit()
	{
		return assumptionsFailed.get() < assumptionLimit;
	}

}