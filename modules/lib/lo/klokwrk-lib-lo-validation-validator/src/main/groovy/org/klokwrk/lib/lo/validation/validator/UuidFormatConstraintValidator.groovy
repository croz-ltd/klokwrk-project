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
import org.klokwrk.lib.lo.validation.constraint.UuidFormatConstraint
import org.klokwrk.lib.xlang.groovy.base.constant.CommonConstants

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.util.regex.Pattern

/**
 * Validates if {@code String} is a well-formed UUID.
 * <p/>
 * Null and empty values are ignored (reported as valid). Validation is based on {@link CommonConstants#REGEX_UUID_FORMAT} regex.
 */
@CompileStatic
class UuidFormatConstraintValidator implements ConstraintValidator<UuidFormatConstraint, String> {
  Pattern uuidFormatPattern = ~CommonConstants.REGEX_UUID_FORMAT

  @Override
  boolean isValid(String uuidToValidate, ConstraintValidatorContext context) {
    // Bean Validation recommends to return true for null and empty values. It is recommended to use other constraints for that.
    if (uuidToValidate == null || uuidToValidate.trim() == "") {
      return true
    }

    if (uuidToValidate ==~ uuidFormatPattern) {
      return true
    }

    return false
  }
}
