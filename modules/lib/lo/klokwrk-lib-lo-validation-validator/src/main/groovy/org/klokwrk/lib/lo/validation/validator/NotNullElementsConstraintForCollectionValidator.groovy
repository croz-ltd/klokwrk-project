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
package org.klokwrk.lib.lo.validation.validator

import groovy.transform.CompileStatic
import org.klokwrk.lib.lo.validation.constraint.NotNullElementsConstraint

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

/**
 * Validates if {@code Collection} contains {@code null} elements.
 * <p/>
 * {@code null} or empty {@code Collections} are ignored (reported as valid).
 */
@CompileStatic
class NotNullElementsConstraintForCollectionValidator implements ConstraintValidator<NotNullElementsConstraint, Collection> {
  @Override
  boolean isValid(Collection collection, ConstraintValidatorContext context) {
    if (collection == null) {
      return true
    }

    if (collection.isEmpty()) {
      return true
    }

    if (collection.contains(null)) {
      return false
    }

    return true
  }
}
