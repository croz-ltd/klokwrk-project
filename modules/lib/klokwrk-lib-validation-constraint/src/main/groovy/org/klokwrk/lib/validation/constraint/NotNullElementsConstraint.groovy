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
 *  The annotated element must not contain {@code null} elements.
 *  <p/>
 *  Accepts {@code Collection} and {@code Map} types. In case of {@code Maps}, both keys and values must be {@code not-null}. Message interpolation key is
 *  {@code org.klokwrk.lib.validation.constraint.NotNullElementsConstraint.message}.
 *  <p/>
 *  {@code null} containers ({@code Collection} and {@code Map}), or empty containers are not reported as violations.
 */
@Documented
@Repeatable(NotNullElementsConstraintList)
@Target([FIELD, METHOD, PARAMETER, TYPE_USE, ANNOTATION_TYPE])
@Retention(RUNTIME)
@Constraint(validatedBy = [])
@interface NotNullElementsConstraint {
  String message() default "{org.klokwrk.lib.validation.constraint.NotNullElementsConstraint.message}"

  Class<?>[] groups() default []
  Class<? extends Payload>[] payload() default []
}

/**
 * Defines several {@link NotNullElementsConstraint} annotations on the same element.
 *
 * @see NotNullElementsConstraint
 */
@Documented
@Target([METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE])
@Retention(RUNTIME)
@interface NotNullElementsConstraintList {
  NotNullElementsConstraint[] value()
}

