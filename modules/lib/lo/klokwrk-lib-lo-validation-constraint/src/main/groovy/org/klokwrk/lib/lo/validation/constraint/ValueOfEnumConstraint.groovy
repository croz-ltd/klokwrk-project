/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2024 CROZ d.o.o, the original author or authors.
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
 * The annotated element must be a string representing a value of given enum (ignoring case).
 * <p/>
 * Constraint accepts only {@code String} types.
 * <p/>
 * Annotation parameter {@code enumClass} is mandatory, while {@code enumNamesSubset} is optional.
 * <p/>
 * When {@code enumNamesSubset} is specified, provided string values must be names (ignoring case) from provided {@code enumClass}.
 * <p/>
 * In default validator implementation, default message interpolation keys (when {@code message} annotation param is empty) are
 * {@code org.klokwrk.lib.lo.validation.constraint.ValueOfEnumConstraint.invalidEnumValueMessage} and {@code org.klokwrk.lib.lo.validation.constraint.ValueOfEnumConstraint.invalidSubsetOfEnumMessage}.
 * <p/>
 * For custom message interpolation (when {@code message} annotation param is configured), default implementation exposes {@code enumClassSimpleName} and {@code enumNamesSubsetList} expressions.
 * <p/>
 * When custom annotation {@code message} parameter value is specified, it can be either a reference to the resource bundle key (must be enclosed in curly braces '<code>{}</code>'), or a hardcoded
 * message. In resource bundle and in the hardcoded message, exposed interpolation expressions must be enclosed in curly braces starting with a dollar sign '<code>${}</code>'.
 */
@Documented
@Repeatable(ValueOfEnumConstraintList)
@Target([FIELD, METHOD, PARAMETER, TYPE_USE, ANNOTATION_TYPE])
@Retention(RUNTIME)
@Constraint(validatedBy = [])
@interface ValueOfEnumConstraint {
  static final String INVALID_ENUM_VALUE_MESSAGE_KEY = "org.klokwrk.lib.lo.validation.constraint.ValueOfEnumConstraint.invalidEnumValueMessage"
  static final String INVALID_SUBSET_OF_ENUM_VALUE_MESSAGE_KEY = "org.klokwrk.lib.lo.validation.constraint.ValueOfEnumConstraint.invalidSubsetOfEnumMessage"

  Class<? extends Enum> enumClass()
  String[] enumNamesSubset() default []

  String message() default ""

  Class<?>[] groups() default []
  Class<? extends Payload>[] payload() default []
}

/**
 * Defines several {@link ValueOfEnumConstraint} annotations on the same element.
 *
 * @see ValueOfEnumConstraint
 */
@Documented
@Target([METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE])
@Retention(RUNTIME)
@interface ValueOfEnumConstraintList {
  ValueOfEnumConstraint[] value()
}
