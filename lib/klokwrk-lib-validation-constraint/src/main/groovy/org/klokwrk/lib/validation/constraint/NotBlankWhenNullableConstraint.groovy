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
 *  The annotated element must must contain at least one non-whitespace character when it is not null.
 *  <p/>
 *  Accepts only {@code String} types. Message interpolation key is {@code org.klokwrk.lib.validation.constraint.NotBlankWhenNullableConstraint.message}.
 *  <p/>
 *  This constraint is very similar to the bean validation's {@code NotBlank}, but this one skips null values and does not report them as violations.
 */
@SuppressWarnings("unused")
@Documented
@Repeatable(NotBlankWhenNullableConstraintList)
@Target([FIELD, METHOD, PARAMETER, TYPE_USE, ANNOTATION_TYPE])
@Retention(RUNTIME)
@Constraint(validatedBy = [])
@interface NotBlankWhenNullableConstraint {
  @SuppressWarnings(["SpaceAfterClosingBrace", "SpaceBeforeClosingBrace"])
  String message() default "{org.klokwrk.lib.validation.constraint.NotBlankWhenNullableConstraint.message}"

  Class<?>[] groups() default []
  Class<? extends Payload>[] payload() default []
}

/**
 * Defines several {@link NotBlankWhenNullableConstraint} annotations on the same element.
 *
 * @see NotBlankWhenNullableConstraint
 */
@Documented
@Target([METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE])
@Retention(RUNTIME)
@interface NotBlankWhenNullableConstraintList {
  NotBlankWhenNullableConstraint[] value()
}
