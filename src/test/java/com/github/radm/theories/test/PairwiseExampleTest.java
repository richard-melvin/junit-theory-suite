package com.github.radm.theories.test;

import org.junit.Test;
import org.junit.contrib.theories.DataPoints;
import org.junit.contrib.theories.Theory;
import org.junit.runner.RunWith;

import com.github.radm.theories.Constraint;
import com.github.radm.theories.Pairwise;
import com.github.radm.theories.TheorySuite;

/**
 * Sample test to demonstrate the use of
 * {@link com.github.radm.theories.TheorySuite} runner. Based on examples from
 * <a href="https://en.wikipedia.org/wiki/All-pairs_testing">wikipedia
 * </a>) and <a href="http://www.cornutum.org/tcases/docs/Tcases-Guide.htm#exampleFind">tcases</a>.
 */
@RunWith(TheorySuite.class)
@Pairwise
@SuppressWarnings("unused")
public class PairwiseExampleTest
{

  enum ChoiceType
  {
    ZERO, ONE, MANY
  }

  enum Category
  {
    A, B, C, D
  }

  @Theory
  public void wikiExample(boolean enabled, ChoiceType choice, Category category)
  {

  }

  public static @DataPoints String[] patterns1 = {"", "a", "abc"};

  public static @DataPoints String[] patterns2 = {" ", "  ", "a c"};

  public static @DataPoints String[] patterns3 = { "literal \"\" ","literal_\"\"_nospace"};


  enum QuoteType
  {
    UNQUOTED, QUOTED
  }

  @Test
  public void findInNonExistantFile()
  {

  }
  @Test
  public void findWithUnBalancedQuotes()
  {

  }

  @Constraint
  public static boolean matchConstraints(ChoiceType linesLongerThanPattern, ChoiceType matchingLines, boolean hasMultipleMatches)
  {
    if (linesLongerThanPattern == ChoiceType.ZERO &&  matchingLines != ChoiceType.ZERO)
    {
      return false;
    }
    if (matchingLines == ChoiceType.ZERO && hasMultipleMatches)
    {
      return false;
    }
    return true;

  }


  @Constraint
  public static boolean mustQuoteIfSpaces(String pattern, QuoteType quoteType)
  {
    if (pattern.contains(" "))
    {
      return quoteType == QuoteType.QUOTED;
    }
    return true;

  }

  @Theory
  public void findInFile(ChoiceType linesLongerThanPattern, ChoiceType matchingLines, boolean hasMultipleMatches,
      String pattern, QuoteType quoteType)
  {

  }


}
