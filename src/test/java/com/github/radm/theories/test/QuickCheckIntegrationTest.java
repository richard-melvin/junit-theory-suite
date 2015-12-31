package com.github.radm.theories.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.contrib.theories.Theory;
import org.junit.runner.RunWith;

import com.github.radm.theories.TheorySuite;
import com.pholser.junit.quickcheck.ForAll;
import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.generator.Fields;

@RunWith(TheorySuite.class)
/**
 * test the examples from https://github.com/pholser/junit-quickcheck
 */
@SuppressWarnings("deprecation")
public class QuickCheckIntegrationTest
{

  @Theory
  public void concatenationLength(@ForAll(sampleSize = 20) String s1, @ForAll(sampleSize = 20) String s2)
  {
    assertEquals(s1.length() + s2.length(), (s1 + s2).length());
  }

  public static class Point
  {
    public double x;

    public double y;

    public double z;

    public String toString()
    {
      return String.format("%g, %g, %g", x, y, z);
    }
  }

  @Theory
  public void originDistance(@ForAll @From(Fields.class) Point p)
  {

    assertTrue(Math.sqrt(p.x * p.x + p.y * p.y + p.z * p.z) > 0);
  }

}
