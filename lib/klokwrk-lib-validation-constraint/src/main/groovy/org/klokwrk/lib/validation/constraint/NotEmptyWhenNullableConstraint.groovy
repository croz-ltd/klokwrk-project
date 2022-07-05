package org.klokwrk.lib.validation.constraint

import javax.validation.Constraint
import javax.validation.Payload
import java.lang.annotation.Documented
import java.lang.annotation.Repeatable
import java.lang.annotation.Retention
import java.lang.annotation.Target

import static java.lang.annotation.ElementType.ANNOTATION_TYPE
import static java.lang.annotation.ElementType.CONSTRUCTOR
import static java.lang.annotation.ElementType.FIELD
import static java.lang.annotation.ElementType.METHOD
import static java.lang.annotation.ElementType.PARAMETER
import static java.lang.annotation.ElementType.TYPE_USE
import static java.lang.annotation.RetentionPolicy.RUNTIME

/**
 *  The annotated element must not be empty when it is not {@code null}.
 *  <p/>
 *  Accepts {@code Collection} and {@code Map} types. Message interpolation key is {@code org.klokwrk.lib.validation.constraint.NotEmptyWhenNullableConstraint.message}.
 *  <p/>
 *  This constraint is very similar to the standard bean validation's {@code NotEmpty}, but this one allows for {@code null} containers and does not report them as violations.
 */
@Documented
@Repeatable(NotEmptyWhenNullableConstraintList)
@Target([FIELD, METHOD, PARAMETER, TYPE_USE, ANNOTATION_TYPE])
@Retention(RUNTIME)
@Constraint(validatedBy = [])
@interface NotEmptyWhenNullableConstraint {
  String message() default "{org.klokwrk.lib.validation.constraint.NotEmptyWhenNullableConstraint.message}"

  Class<?>[] groups() default []
  Class<? extends Payload>[] payload() default []
}

/**
 * Defines several {@link NotEmptyWhenNullableConstraint} annotations on the same element.
 *
 * @see NotEmptyWhenNullableConstraint
 */
@Documented
@Target([METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE])
@Retention(RUNTIME)
@interface NotEmptyWhenNullableConstraintList {
  NotEmptyWhenNullableConstraint[] value()
}
