package com.github.radm.theories.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import org.junit.contrib.theories.DataPoint;
import org.junit.contrib.theories.Theory;
import org.junit.runner.RunWith;

import com.github.radm.theories.Constraint;
import com.github.radm.theories.Pairwise;
import com.github.radm.theories.TheorySuite;
import com.github.radm.theories.WithConstraints;

/**
 * EqualsHashCodeTest - originally from
 * <a href="http://stackoverflow.com/questions/837484/junit-theory-for-hashcode-equals-contract">here</a>
 */
@RunWith(TheorySuite.class)
@SuppressWarnings("unused")
public class EqualsHashCodeTest
{

  // a class that nothing will ever be of
  private class Dummy {

  }

  @DataPoint
  public static String a = "a";
  @DataPoint
  public static String b = "b";
  @DataPoint
  public static String emptyString = "";


  @Theory
  public void equalsIsReflexive(Object o)
  {

    assertTrue(o.equals(o));
  }

  @Constraint("equals")
  public static boolean pairsEquals(Object x,
      Object y)
  {
    return x.equals(y);
  }

  @Theory
  @WithConstraints("equals")
  public void equalsIsSymmetric(Object x, Object y)
  {
    assertTrue(x.equals(y));
  }


  @Theory
  @Pairwise
  @WithConstraints("equals")
  public void equalsIsTransitive(Object x,
      Object y, Object z)
  {

    assertTrue(z.equals(x));
  }

  @Theory
  public void equalsReturnsFalseOnNull(Object x)
  {
    assertFalse(x.equals(null));
  }

  @Theory
  public void equalsReturnsFalseOnAnotherClass(Object x)
  {
    assertFalse(x.equals(new Dummy()));
  }


  @Theory
  public void equalsIsRepeatable(Object x, Object y)
  {
    boolean alwaysTheSame = x.equals(y);

    for (int i = 0; i < 30; i++)
      assertThat(x.equals(y), is(alwaysTheSame));
  }

  @Theory
  public void hashCodeIsRepeatable(Object x)
  {
    int alwaysTheSame = x.hashCode();

    for (int i = 0; i < 30; i++)
      assertThat(x.hashCode(), is(alwaysTheSame));
  }

  @Theory
  @WithConstraints("equals")
  public void hashCodeIsConsistentWithEquals(Object x, Object y)
  {

    assertEquals(x.hashCode(), y.hashCode());
  }


}
