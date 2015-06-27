package com.github.radm.theories;


import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * A constraint is a public static boolean function with one or more arguments.
 *
 * For each set of potential argument values to a theory function, those failing a constraint will be discarded
 * before the function is called.
 *
 * Constraints with no value set are considered global, and apply to all theories that have a sequence of arguments of matching type.
 * Other constraints are only used when referenced by the annotation {@link WithConstraints}.
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface Constraint {
    String value() default "";

}
