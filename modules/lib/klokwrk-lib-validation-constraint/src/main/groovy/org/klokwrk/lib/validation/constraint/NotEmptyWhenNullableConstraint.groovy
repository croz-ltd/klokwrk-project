/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 CROZ d.o.o, the original author or authors.
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
