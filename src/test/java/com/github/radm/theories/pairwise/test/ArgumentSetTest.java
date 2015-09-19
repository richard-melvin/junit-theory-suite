package com.github.radm.theories.pairwise.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.contrib.theories.DataPoint;
import org.junit.contrib.theories.Theories;
import org.junit.contrib.theories.Theory;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.radm.theories.pairwise.ArgVector;
import com.github.radm.theories.pairwise.ArgumentSet;

/**
 * Common test data for all types of iterator.
 */
@RunWith(Theories.class)
@SuppressWarnings("javadoc")
public abstract class ArgumentSetTest {

    private static final Logger LOG = LoggerFactory.getLogger(ArgumentSetTest.class);

    public static @DataPoint ArgumentSet oneBoolean = new ArgumentSet(Arrays.asList("a"),
            Arrays.asList(Arrays.asList(true, false)));

    public static @DataPoint ArgumentSet twoBooleans = makeTwoBooleans();

    private static ArgumentSet makeTwoBooleans() {
        return new ArgumentSet(Arrays.asList("a", "b"),
                Arrays.asList(Arrays.asList(true, false), Arrays.asList(false, true)));
    }

    public static @DataPoint ArgumentSet threeIntegers = makeThreeIntegers();

    private static ArgumentSet makeThreeIntegers() {
        return new ArgumentSet(Arrays.asList("a", "b", "c"),
                Arrays.asList(Arrays.asList(1, 2, 3), Arrays.asList(1, 2, 3), Arrays.asList(1, 2, 3)));
    }

    public static @DataPoint ArgumentSet fourDays = makeFourDays();

    private static ArgumentSet makeFourDays() {
        return ArgumentSet.fromArray(Arrays.asList("a", "b", "c", "d"),
                Arrays.asList(DayOfWeek.values(), DayOfWeek.values(), DayOfWeek.values(), DayOfWeek.values()));
    }

    public static @DataPoint ArgumentSet fourWeekDays = makeFourDays()
            .withConstraint("a", args -> isWeekDay((DayOfWeek) args[0]))
            .withConstraint("b", args -> isWeekDay((DayOfWeek) args[1]))
            .withConstraint("c", args -> isWeekDay((DayOfWeek) args[2]))
            .withConstraint("d", args -> isWeekDay((DayOfWeek) args[3]));

    public static @DataPoint ArgumentSet twoBooleansConstrained = makeTwoBooleans().withConstraint("a",
            args -> (Boolean) args[0]);

    public static @DataPoint ArgumentSet alwaysFailingConstraint = makeTwoBooleans().withConstraint("a", args -> false);

    public static @DataPoint ArgumentSet threeIntsConstrained = makeThreeIntegers().withConstraint("c",
            args -> isOdd((Integer) args[0]) && isOdd((Integer) args[1]) && isOdd((Integer) args[2]));

    public static @DataPoint ArgumentSet threeIntsTightlyConstrained = makeThreeIntegers().withConstraint("c",
            args -> 2 == ((Integer) args[0]) && 3 == ((Integer) args[1]) && 1 == ((Integer) args[2]));

    public static @DataPoint ArgumentSet threeIntsConstrainedBySum = makeThreeIntegers()
            .withConstraint("b", args -> 3 <= ((Integer) args[0] + (Integer) args[1]))
            .withConstraint("c", args -> 2 >= ((Integer) args[0] + (Integer) args[2]));

    private static boolean isOdd(int i) {
        return i % 2 != 0;
    }

    private static boolean isWeekDay(DayOfWeek dayOfWeek) {
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }

    @Theory
    public void canIterateWithoutGettingNull(ArgumentSet as) {
        for (ArgVector args : as) {
            assertNotNull(args);
            for (Object o : args.getArgVals()) {
                assertNotNull(o);
            }
        }
    }

    @Theory
    public void canCallHasNextFreely(ArgumentSet as) {

        Iterator<?> iter = as.iterator();

        while (iter.hasNext()) {
            assertTrue(iter.hasNext());
            iter.next();
        }
        assertTrue(!iter.hasNext());

    }

    protected int countByIterator(ArgumentSet as, Iterator<ArgVector> iter) {

        Set<String> seen = new HashSet<>();
        int count = 0;
        while (iter.hasNext()) {
            count++;
            ArgVector next = iter.next();
            String nextStr = next.toString();
            assertTrue(!seen.contains(nextStr));

            LOG.debug("got {}", nextStr);
            seen.add(nextStr);
        }
        LOG.info("length of {} is {}", as.getArgNames(), count);
        return count;
    }

}
