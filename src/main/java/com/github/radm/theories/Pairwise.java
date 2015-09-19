package com.github.radm.theories;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * A pairwise theory is a public non-static void function with three or more
 * arguments.
 *
 * Pairwise theories are tested against a set of possible arguments values that
 * contains, for each pair of arguments, the full range of possible pairs of
 * values .
 *
 */
@Retention(RUNTIME)
@Target({ METHOD, TYPE })
public @interface Pairwise {

}
