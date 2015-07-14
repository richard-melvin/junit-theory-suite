package com.github.radm.theories.test;

import org.junit.contrib.theories.Theory;
import org.junit.runner.RunWith;

import com.github.radm.theories.Pairwise;
import com.github.radm.theories.TheorySuite;

/**
 * Sample test to demonstrate the use of
 * {@link com.github.radm.theories.TheorySuite} runner. Based on
 * <a href="https://en.wikipedia.org/wiki/All-pairs_testing">wikipedia
 * example</a>).
 */
@RunWith(TheorySuite.class)
@Pairwise
@SuppressWarnings("unused")
public class PairwiseExampleTest {

	enum ChoiceType {
		ONE, TWO, THREE
	}

	enum Category {
		A, B, C, D
	}

	@Theory
	public void wikiExample(boolean enabled, ChoiceType choice, Category category) {

	}

}
