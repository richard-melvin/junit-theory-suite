# Junit Theories by Example

This tutorial walks through some examples of using JUnit theories to effectively test complex code.

The Java 8 `java.time` api is used as the code to be tested. Not so much because it needs testing,
but because calendar handling is a rich source of special cases and glitches. Real tests would more likely be of code that used that api.

The full source code for these examples is [here](../src/test/java/com/github/radm/theories/test/ExampleTest.java)

The examples rely on Java 8 and junit 4.12.

## Basic JUnit test

In JUnit, a test case is just a function annotated with `@Test' and no arguments.

```java

	@Test
	public void twentyTwelveIsLeapYear() {
		assertTrue(Year.of(2012).isLeap());
	}
```

This bundles together the test data and the checking logic, which is of
course the right thing to do if the two depend on each other, as above.

## Cover multiple test cases with a single Theory

One test is rarely enough, and creating multiple similar tests by simple cut and paste is little better.
One way of defining multiple related tests cases is to use a
[parameterised test runner](https://github.com/junit-team/junit/wiki/Parameterized-tests).
JUnit [theories](https://github.com/junit-team/junit/wiki/Theories) are an alternative approach that is both more concise and more powerful.

In JUnit, the `Theory` annotation marks a single _test function_ as supporting multiple
_test cases_. The details of the test data are passed as arguments directly to the test.

```java


	@Theory
	public void leapYearsHaveMoreDaysInFebruary(Year year) {
		if (year.isLeap()) {
			assertEquals(Month.FEBRUARY.maxLength(),
					year.atMonth(Month.FEBRUARY).lengthOfMonth());
		} else {
			assertEquals(Month.FEBRUARY.minLength(),
					year.atMonth(Month.FEBRUARY).lengthOfMonth());
		}
	}
```
The idea of a theory is that it should pass for any possible argument value, just as a test with no arguments should always pass.
So, unlike a simple test, it should not have any logic in it that is specific to any one test case.
Ideally you would test it with every possible value. In practice, given you presumably want your tests to complete
within the lifetime of the universe, you have to have use some means of specifying the actual set of values it will be tested on.

The most straightforward way to do that is with data members (or methods) of
the test class annotated with `DataPoint` or `DataPoints`.

```java

	@DataPoints
	public static List<Year> years = IntStream.range(1995, 2016).boxed()
			.map(Year::of).collect(Collectors.toList());
```

The above example uses Java 8 streams to build a list of the years 1995 to 2015. The theory is then tested for every year in that range, where obviously it should always pass.

By default, datapoints are matched to arguments based on type; for `Datapoint` it should be the same type, for `Datapoints` is should be a collection or array of argument values.

Named datapoint sets are also supported:

	@DataPoints("thisCentury")
	public static List<Year> years = IntStream.range(2000, 2100).boxed()
			.map(Year::of).collect(Collectors.toList());

These can be tied to a particular test argument by a `FromDataPoints` annotation with corresponding name.


## Theories with multiple arguments

When a theory has multiple arguments, all possible combinations of arguments are exercised.

```java

	@Theory
	public void endOfMonthAlwaysMatchesMonthLength(Year year, Month month) {
		LocalDate atEndOfMonth = year.atMonth(month).atEndOfMonth();
		assertEquals(month.minLength(), atEndOfMonth.getDayOfMonth());
	}
```

The above code will be executed for every combination of year and month; 240 test cases in 3 lines of code.
It will pass for almost all those cases, but fail for February in leap years. The corrected code is:

```java

	@Theory
	public void endOfMonthAlwaysWithinRange(Year year, Month month) {
		LocalDate atEndOfMonth = year.atMonth(month).atEndOfMonth();
		assertTrue(month.maxLength() >= atEndOfMonth.getDayOfMonth());
		assertTrue(month.minLength() <= atEndOfMonth.getDayOfMonth());

	}
```

Another feature shown by the above example is that, as `Month` is an enum, no
explicit `Datapoint` annotation was needed - it defaults to using all possible values.
The same is true of boolean theory arguments.

If a test has several arguments of the same type, the same value generation logic is applied to each such argument:

```java

	@Theory
	public void yearOrderingMatchesDayOrdering(Year yearOne, Year yearTwo) {

		assumeTrue(!yearOne.equals(yearTwo));

		if (yearOne.compareTo(yearTwo) > 0)
		{
			assertTrue(yearOne.atDay(1).compareTo(yearTwo.atMonth(Month.DECEMBER).atEndOfMonth()) > 0);
			assertTrue(yearOne.atMonth(Month.DECEMBER).atEndOfMonth().compareTo(yearTwo.atDay(1)) > 0);
		}
		else
		{
			assertTrue(yearOne.atDay(1).compareTo(yearTwo.atMonth(Month.DECEMBER).atEndOfMonth()) < 0);
			assertTrue(yearOne.atMonth(Month.DECEMBER).atEndOfMonth().compareTo(yearTwo.atDay(1)) < 0);
		}
	}
```

## Discarding test cases for a particular theory

Sometimes, some of the datapoints specified for a test in general are not applicable for a particular test.

``` java

	@DataPoints
	public static int[] monthDays = IntStream.range(1, 31).toArray();

	@Theory
	public void previousDayBySubtraction(Year year, int monthDay) {
		YearMonth febForYear = year.atMonth(Month.FEBRUARY);
		assumeTrue(monthDay <= febForYear.lengthOfMonth());
		DayOfWeek day = febForYear.atDay(monthDay).getDayOfWeek();
		assertTrue(febForYear.atDay(monthDay).minusDays(1).getDayOfWeek() == day
				.minus(1));
	}

```

In such cases, instead of fiddling with named datapoints, `Assume.assumeTrue` can be used to discard those points.
Unlike other assertion methods, as long as some test cases pass the assumption, the overall test passes.

