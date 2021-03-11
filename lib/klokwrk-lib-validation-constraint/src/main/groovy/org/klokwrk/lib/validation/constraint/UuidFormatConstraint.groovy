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
 * The annotated element must be well-formed UUID string.
 * <p/>
 * Accepts only {@code String} types. Message interpolation key is {@code org.klokwrk.lib.validation.constraint.UuidFormatConstraint.message}.
 */
@SuppressWarnings("unused")
@Documented
@Repeatable(UuidFormatConstraintList)
@Target([FIELD, METHOD, PARAMETER, TYPE_USE, ANNOTATION_TYPE])
@Retention(RUNTIME)
@Constraint(validatedBy = [])
@interface UuidFormatConstraint {
  @SuppressWarnings(["SpaceAfterClosingBrace", "SpaceBeforeClosingBrace"])
  String message() default "{org.klokwrk.lib.validation.constraint.UuidFormatConstraint.message}"

  Class<?>[] groups() default []
  Class<? extends Payload>[] payload() default []
}

/**
 * Defines several {@link UuidFormatConstraint} annotations on the same element.
 *
 * @see UuidFormatConstraint
 */
@Documented
@Target([METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE])
@Retention(RUNTIME)
@interface UuidFormatConstraintList {
  UuidFormatConstraint[] value()
}
