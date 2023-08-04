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
import org.klokwrk.lib.xlang.groovy.base.constant.CommonConstants
import org.klokwrk.lib.lo.validation.constraint.UnLoCodeFormatConstraint

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.util.regex.Pattern

/**
 * Validates if {@code String} is a well-formed UN/LOCODE.
 * <p/>
 * Null and empty values are ignored (reported as valid). Validation is based on {@link CommonConstants#REGEX_UN_LO_CODE} regex.
 */
@CompileStatic
class UnLoCodeFormatConstraintValidator implements ConstraintValidator<UnLoCodeFormatConstraint, String> {
  Pattern unLoCodeFormatPattern = ~CommonConstants.REGEX_UN_LO_CODE

  @Override
  boolean isValid(String unLoCodeToValidate, ConstraintValidatorContext context) {
    // Bean Validation recommends to return true for null and empty values. It is recommended to use other constraints for that.
    if (unLoCodeToValidate == null || unLoCodeToValidate.trim() == "") {
      return true
    }

    if (unLoCodeToValidate ==~ unLoCodeFormatPattern) {
      return true
    }

    return false
  }
}
