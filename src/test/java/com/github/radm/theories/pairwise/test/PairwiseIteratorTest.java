package com.github.radm.theories.pairwise.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Test;
import org.junit.contrib.theories.Theories;
import org.junit.runner.RunWith;

import com.github.radm.theories.pairwise.ArgVector;
import com.github.radm.theories.pairwise.ArgumentSet;

/**
 * unit test of ArgumentSet. Note uses old theories runner to be safe.
 */
@SuppressWarnings("javadoc")
@RunWith(Theories.class)
public class PairwiseIteratorTest extends ArgumentSetTest {

  @Test
  public void expectedLengthForOneBoolean() {
    assertEquals(2, countArguments(oneBoolean));
  }

  @Test
  public void expectedLengthForTwoBooleans() {
    assertEquals(4, countArguments(twoBooleans));
  }

  @Test
  public void expectedLengthForThreeIntegers() {
    assertTrue(9 == countArguments(threeIntegers));
  }

  @Test
  public void expectedLengthForTwoBooleansConstrained() {
    assertEquals(2, countArguments(twoBooleansConstrained));
  }

  @Test
  public void expectedLengthForThreeIntsConstrained() {
    assertEquals(5, countArguments(threeIntsConstrained));
  }

  @Test
  public void expectedLengthForFourDays() {
    assertEquals(63, countArguments(fourDays));
  }

  @Test
  public void expectedLengthForFourWeekDays() {
    assertEquals(36, countArguments(fourWeekDays));
  }


  @Test
  public void expectedLengthForAlwaysFailingConstraint() {
    assertEquals(0, countArguments(alwaysFailingConstraint));
  }


  @Test
  public void expectedLengthForTightConstraint() {
    assertEquals(1, countArguments(threeIntsTightlyConstrained));
  }


  @Test
  public void expectedLengthForThreeIntsConstrainedBySum() {
    assertEquals(2, countArguments(threeIntsConstrainedBySum));
  }


  private int countArguments(ArgumentSet as) {
    Iterator<ArgVector> iter = as.pairwiseIterator();

    return countByIterator(as, iter);
  }

}
