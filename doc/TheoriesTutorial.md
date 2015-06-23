# Junit Theories by Example

This tutorial walks through some examples of using JUnit theories to effectively test complex code.

The Java 8 `java.time` api is used as the code to be tested. Not because it needs testing,
but because calendar handling is a rich source of special cases and glitches. In practise, real tests would more likely be of code that used that api.

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

One way of defining multiple related tests cases is to use a parameterised test runner; theories are an alternative that is both more concise and more powerful.

In JUnit, the `Theory` annotation marks a single test function as supporting multiple test cases; the details of the test data are passed as arguments directly to the test.

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
The principle of a theory is that it should pass for any argument value. So, unlike the previous example, it should
not have any logic in it specific to any one test case. Ideally you would test it with every possible value, in
practise, given you presumably want your tests to complete within the lifetime of the universe, you have to specify the
set of actual values it will be tested on.

The most straightforward way to do that is
with data members (or methods) of the test class annotated with `DataPoint` or `DataPoints`.

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

The above code will be executed for every combination of year and month; 240 test cases in 3 lines of code. It will pass for almost all those cases, but fail for February in leap years.	The corrected code is:

```java

	@Theory
	public void endOfMonthAlwaysWithinRange(Year year, Month month) {
		LocalDate atEndOfMonth = year.atMonth(month).atEndOfMonth();
		assertTrue(month.maxLength() >= atEndOfMonth.getDayOfMonth());
		assertTrue(month.minLength() <= atEndOfMonth.getDayOfMonth());

	}
```

Another feature shown by the above example is that, as `Month` is an enum, no explicit `Datapoint` annotation was needed - it default to using all possible values. The same is true of boolean theory arguments.

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

In such cases, `Assume.assumeTrue` can be used to discard those points. Unlike other assertion methods, as long as some test cases pass the assumption, the overall test passes.

