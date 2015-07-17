package com.github.radm.theories.runner.test;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.runner.Computer;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunListener;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.github.radm.theories.TheorySuite;

/**
 * Handle some details of testing custom test runners.
 */
@SuppressWarnings("javadoc")
public abstract class CustomRunnerTest {

  public static final Computer runSelect = new Computer() {
      @Override
      protected Runner getRunner(RunnerBuilder builder, Class<?> testClass)
          throws Throwable {
        return new TheorySuite(testClass);
      }
    };

  public CustomRunnerTest() {
    super();
  }

  protected RunListener runTestCaseWithMockListener(Class<?> testCase, Description subCase) throws InitializationError {
    RunListener listener = Mockito.mock(RunListener.class);
    JUnitCore core = new JUnitCore();
    core.addListener(listener);
    core.run(Request.runner(new TheorySuite(testCase)).filterWith(subCase));
    return listener;
  }


  protected RunListener runTestWithMockListener(Class<?> testCase) {
    RunListener listener = Mockito.mock(RunListener.class);
    JUnitCore core = new JUnitCore();
    core.addListener(listener);
    core.run(runSelect, testCase);
    return listener;
  }

  protected void alwaysPassesWithCases(RunListener listener, int expected) throws Exception {
    verify(listener, times(0)).testAssumptionFailure(Matchers.any());
    verify(listener, times(0)).testFailure(Matchers.any());
    verify(listener, times(expected)).testStarted(Matchers.any());
      verify(listener, times(expected)).testFinished(Matchers.any());
  }

}