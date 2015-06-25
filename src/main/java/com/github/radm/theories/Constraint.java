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
 * Theory arguments are matched to constraint arguments by the same rules used to match theory arguments to datapoints.
 * So arguments of a constraint function can be annotated with FromDatapoint to deal with the cases where the type alone is insufficient.
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface Constraint {

}
