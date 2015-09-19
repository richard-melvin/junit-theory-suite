## junit-theory-suite - an alternative test runner for JUnit theories

[![Build Status](https://travis-ci.org/richard-melvin/junit-theory-suite.svg?branch=master)](https://travis-ci.org/richard-melvin/junit-theory-suite) [![Release](https://img.shields.io/github/release/richard-melvin/junit-theory-suite.svg?label=JitPack)](https://img.shields.io/github/release/richard-melvin/junit-theory-suite.svg?label=JitPack) [![License](http://img.shields.io/:license-mit-blue.svg)](http://doge.mit-license.org)



junit-theory-suite is a simple library that replaces the default [JUnit](https://github.com/junit-team/junit)
[theories](https://github.com/junit-team/junit/wiki/Theories) runner. The key improvements are:

- treats each individual combination of parameters as a distinct test case.
- optionally select [All-Pairs](https://en.wikipedia.org/wiki/All-pairs_testing) test case selection instead of the default exhaustive
- specify constraints between argument values that discard test cases that don't match the constraint

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

![Eclipse runner](doc/runner.png?raw=true)

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
      <version>0.7.2</version>
      <scope>test</scope>
  </dependency>

```


## junit-theory-suite

junit-theory-suite uses a version of the JUnit theories runner that has been modified to respect generics on theory parameter types, as described [here](https://github.com/junit-team/junit/issues/64). The classes that comprise this rendition of the JUnit theories runner are packaged as org.junit.contrib.theories.*, rather than org.junit.experimental.theories.*. Be sure to use the contrib version of the runner, annotations, etc. with junit-theory-suite.

## Known differences to the standard theories runner

1. `@Before` and `@After` are run before each individual test case, not test method.
2. The JUnit rule `TestName` reports the test case with the argument values filled in, e.g. `theoryOnYearAndMonth(1995,OCTOBER)`


## More information

Tutorial documentation for JUnit theories is [here](doc/TheoriesTutorial.md).


## Related tools

[junit-quickcheck](https://github.com/pholser/junit-quickcheck) works with junit-theory-suite to generate random parameter values;
see [QuickCheckIntegrationTest](src/test/java/com/github/radm/theories/test/QuickCheckIntegrationTest.java).

[pitest](https://github.com/hcoles/pitest) provides mutation test coverage, which is very useful when doing theory-based testing to confirm you are actually properly testing the software and not just exercising it.

[jcunit](https://github.com/dakusui/jcunit) does is an alternative approach to combinatorial testing using junit.

[slf4j](http://www.slf4j.org/) is used for logging; this is the only dependency other than junit itself.

[jitpack.io](https://jitpack.io/) is used as a lightweight alternative to releasing to maven central.


