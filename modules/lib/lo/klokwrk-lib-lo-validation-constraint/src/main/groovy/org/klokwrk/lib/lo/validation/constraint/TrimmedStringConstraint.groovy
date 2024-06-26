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
import static java.lang.annotation.ElementType.FIELD
import static java.lang.annotation.ElementType.METHOD
import static java.lang.annotation.ElementType.PARAMETER
import static java.lang.annotation.ElementType.TYPE_USE
import static java.lang.annotation.RetentionPolicy.RUNTIME

/**
 * The annotated element must be a string without any spaces in front or at the end.
 * <p/>
 * Accepts only {@code String} types.
 * <p/>
 * Default message interpolation key is {@code org.klokwrk.lib.lo.validation.constraint.NotBlankWhenNullableConstraint.message}.
 */
@Documented
@Repeatable(TrimmedStringConstraintList)
@Target([FIELD, METHOD, PARAMETER, TYPE_USE, ANNOTATION_TYPE])
@Retention(RUNTIME)
@Constraint(validatedBy = [])
@interface TrimmedStringConstraint {
  String message() default "{org.klokwrk.lib.lo.validation.constraint.TrimmedStringConstraint.message}"

  Class<?>[] groups() default []
  Class<? extends Payload>[] payload() default []
}

/**
 * Defines several {@link TrimmedStringConstraint} annotations on the same element.
 *
 * @see TrimmedStringConstraint
 */
@interface TrimmedStringConstraintList {
  TrimmedStringConstraint[] value()
}
