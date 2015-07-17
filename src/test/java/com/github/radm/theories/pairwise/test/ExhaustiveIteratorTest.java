package com.github.radm.theories.pairwise.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.radm.theories.pairwise.ArgumentSet;

@SuppressWarnings("javadoc")
public class ExhaustiveIteratorTest extends ArgumentSetTest {

  @Test
  public void expectedLength1() {
    assertEquals(2, countArguments(oneBoolean));
  }

  @Test
  public void expectedLength2() {
    assertEquals(4, countArguments(twoBooleans));
  }

  @Test
  public void expectedLength3() {
    assertEquals(27, countArguments(threeIntegers));
  }

  @Test
  public void expectedLength4() {
    assertEquals(2, countArguments(twoBooleansConstrained));
  }

  @Test
  public void expectedLength5() {
    assertEquals(8, countArguments(threeIntsConstrained));
  }

  @Test
  public void expectedLength6() {
    assertEquals(7 * 7 * 7 * 7, countArguments(fourDays));
  }

  @Test
  public void expectedLength7() {
    assertEquals(5 * 5 * 5 * 5, countArguments(fourWeekDays));
  }



  protected int countArguments(ArgumentSet as) {

    return countByIterator(as, as.iterator());
  }


}
