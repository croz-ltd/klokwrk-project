/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 CROZ d.o.o, the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.klokwrk.lib.lo.validation.constraint

import jakarta.validation.Constraint
import jakarta.validation.Payload
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
 * This constraint accepts only {@code String} types.
 * <p/>
 * In default validator implementation, default message interpolation keys (when {@code message} annotation param is empty) are
 * {@code org.klokwrk.lib.lo.validation.constraint.RandomUuidFormatConstraint.invalidRandomUuidFormatMessage} and
 * {@code org.klokwrk.lib.lo.validation.constraint.RandomUuidFormatConstraint.invalidUuidFormatMessage}. Default implementation uses
 * {@code org.klokwrk.lib.lo.validation.constraint.RandomUuidFormatConstraint.invalidUuidFormatMessage} when corresponding string cannot be parsed into a UUID. Similarly, default implementation uses
 * {@code org.klokwrk.lib.lo.validation.constraint.RandomUuidFormatConstraint.invalidRandomUuidFormatMessage} when corresponding string does represent an UUID but is not a random UUID (version 4 and
 * variant 2).
 * <p/>
 * When custom annotation {@code message} parameter value is specified, it can be either a reference to the resource bundle key (must be enclosed in curly braces '<code>{}</code>'), or a hardcoded
 * message. In resource bundle and in the hardcoded message, interpolation expressions must be enclosed in curly braces starting with a dollar sign '<code>${}</code>'.
 */
@Documented
@Repeatable(RandomUuidFormatConstraintList)
@Target([FIELD, METHOD, PARAMETER, TYPE_USE, ANNOTATION_TYPE])
@Retention(RUNTIME)
@Constraint(validatedBy = [])
@interface RandomUuidFormatConstraint {
  static final String INVALID_UUID_FORMAT_MESSAGE_KEY = "org.klokwrk.lib.lo.validation.constraint.RandomUuidFormatConstraint.invalidUuidFormatMessage"
  static final String INVALID_RANDOM_UUID_FORMAT_MESSAGE_KEY = "org.klokwrk.lib.lo.validation.constraint.RandomUuidFormatConstraint.invalidRandomUuidFormatMessage"

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
