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
package org.klokwrk.lib.validation.validator

import groovy.transform.CompileStatic
import org.klokwrk.lib.validation.constraint.NotNullElementsConstraint

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

/**
 * Validates if {@code Map} instance contains {@code null} keys and/or values.
 * <p/>
 * {@code null} or empty {@code Maps} are ignored (reported as valid).
 */
@CompileStatic
class NotNullElementsConstraintForMapValidator implements ConstraintValidator<NotNullElementsConstraint, Map> {
  @Override
  boolean isValid(Map map, ConstraintValidatorContext context) {
    if (map == null) {
      return true
    }

    if (map.isEmpty()) {
      return true
    }

    if (map.containsKey(null)) {
      return false
    }

    if (map.containsValue(null)) {
      return false
    }

    return true
  }
}
