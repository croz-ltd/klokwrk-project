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
import org.klokwrk.lib.validation.constraint.TrimmedStringConstraint

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

@CompileStatic
class TrimmedStringConstraintValidator implements ConstraintValidator<TrimmedStringConstraint, String>  {
  @Override
  boolean isValid(String stringToValidate, ConstraintValidatorContext context) {
    // Bean Validation recommends to return true for null and empty values. It is recommended to use other constraints for that.
    if (stringToValidate == null || stringToValidate.trim() == "") {
      return true
    }

    if (stringToValidate == stringToValidate.trim()) {
      return true
    }

    return false
  }
}
