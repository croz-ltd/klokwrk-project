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
 * The annotated element must be well-formed random UUID string.
 * <p/>
 * The version of random UUID string is 4, and the variant is 2. In the following UUID string representation, character M must have a value of 4 (version), while character N must have a value between
 * 8 and B (variant):
 * <pre>
 *   4dd180d4-c554-4235-85be-3dfed42c316d
 *                 |    |
 *   xxxxxxxx-xxxx-Mxxx-Nxxx-xxxxxxxxxxxx
 * </pre>
 * For more detailed explanation, please take a look at https://www.uuidtools.com/decode
 * <p/>
 * This constraint accepts only {@code String} types. Message interpolation keys are {@code org.klokwrk.lib.validation.constraint.RandomUuidFormatConstraint.invalidRandomUuidFormatMessage} and
 * {@code org.klokwrk.lib.validation.constraint.RandomUuidFormatConstraint.invalidUuidFormatMessage}.
 * <p/>
 * Implementation should use {@code org.klokwrk.lib.validation.constraint.RandomUuidFormatConstraint.invalidUuidFormatMessage} when corresponding string cannot be parsed into a UUID.
 * <p/>
 * Implementation should use {@code org.klokwrk.lib.validation.constraint.RandomUuidFormatConstraint.invalidRandomUuidFormatMessage} when corresponding string does represent an UUID but is not a
 * random UUID (version 4 and variant 2).
 * <p/>
 * To avoid generation of constraint violation with default empty message, concrete validator implementation should disable default constraint violation with
 * {@code Context.disableDefaultConstraintViolation()}
 */
@Documented
@Repeatable(RandomUuidFormatConstraintList)
@Target([FIELD, METHOD, PARAMETER, TYPE_USE, ANNOTATION_TYPE])
@Retention(RUNTIME)
@Constraint(validatedBy = [])
@interface RandomUuidFormatConstraint {
  static final String INVALID_UUID_FORMAT_MESSAGE_KEY = "org.klokwrk.lib.validation.constraint.RandomUuidFormatConstraint.invalidUuidFormatMessage"
  static final String INVALID_RANDOM_UUID_FORMAT_MESSAGE_KEY = "org.klokwrk.lib.validation.constraint.RandomUuidFormatConstraint.invalidRandomUuidFormatMessage"

  // Must be specified, but is not used. See implementation notes at the end of annotation description in groovydoc.
  String message() default ""

  Class<?>[] groups() default []
  Class<? extends Payload>[] payload() default []
}

/**
 * Defines several {@link RandomUuidFormatConstraint} annotations on the same element.
 *
 * @see RandomUuidFormatConstraint
 */
@Documented
@Target([METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE])
@Retention(RUNTIME)
@interface RandomUuidFormatConstraintList {
  RandomUuidFormatConstraint[] value()
}
