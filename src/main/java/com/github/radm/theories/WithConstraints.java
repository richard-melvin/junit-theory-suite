package com.github.radm.theories;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Associate a theory with a specific set of named {@link Constraint}. Arguments
 * of the constraint must be a sub-sequence of the arguments of the theory.
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface WithConstraints
{

  /**
   *
   * @return the exlicit set of constraint names to use
   */
  String[]value();

}
