## junit-theory-suite - an alternative test runner for JUnit theories

[![Build Status](https://travis-ci.org/richard-melvin/junit-theory-suite.svg?branch=master)](https://travis-ci.org/richard-melvin/junit-theory-suite) [![Release](https://img.shields.io/github/release/richard-melvin/junit-theory-suite.svg?label=JitPack)](https://img.shields.io/github/release/richard-melvin/junit-theory-suite.svg?label=JitPack)


junit-theory-suite is a simple library that replaces the default [JUnit](https://github.com/junit-team/junit)
[theories](https://github.com/junit-team/junit/wiki/Theories) runner with
one that treats each individual combination of parameters as a distinct test case.

```java
import com.github.radm.theories.TheorySuite;

@RunWith(TheorySuite.class)
public class ExampleTest {

	@DataPoints
	public static List<Year> years = IntStream.range(1995, 1999).boxed()
			.map(Year::of).collect(Collectors.toList());

	@Theory
	public void theoryOnYearOnly(Year year) {
		if (year.isLeap()) {
			assertEquals(Month.FEBRUARY.maxLength(),
					year.atMonth(Month.FEBRUARY).lengthOfMonth());
		} else {
			assertEquals(Month.FEBRUARY.minLength(),
					year.atMonth(Month.FEBRUARY).lengthOfMonth());
		}
	}

```

will produce junit XML results:

```xml
  <testcase classname="com.github.radm.theories.test.ExampleTest" name="theoryOnYearOnly(1995)" time="0"/>
  <testcase classname="com.github.radm.theories.test.ExampleTest" name="theoryOnYearOnly(1996)" time="0"/>
  <testcase classname="com.github.radm.theories.test.ExampleTest" name="theoryOnYearOnly(1997)" time="0"/>
  <testcase classname="com.github.radm.theories.test.ExampleTest" name="theoryOnYearOnly(1998)" time="0.001"/>
```

The individual test cases are also visible in IDE test runners such as Eclipse.

![Eclipse runner](runner.png?raw=true)

To use it, simply:


- replace `@RunWith(Theories.class)` with `@RunWith(TheorySuite.class)`

- add the following lines to the appropriate parts of pom.xml (for maven):

```xml

	<repository>
	    <id>jitpack.io</id>
	    <url>https://jitpack.io</url>
	</repository>


	<dependency>
	    <groupId>com.github.richard-melvin</groupId>
	    <artifactId>junit-theory-suite</artifactId>
	    <version>v0.3.0</version>
	</dependency>

```

This uses [jitpack.io](https://jitpack.io/) as a lightweight alternative to releasing to maven central.

## Known differences to the standard theories runner

1. `@Before` and `@After` are run before each individual test case, not test method.
2. The JUnit rule `TestName` reports the test case with the argument values filled in, e.g. `theoryOnYearAndMonth(1995,OCTOBER)`




