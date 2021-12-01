/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 CROZ d.o.o, the original author or authors.
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
 * The annotated element must be well-formed UN/LOCODE string.
 * <p/>
 * Accepts only {@code String} types. Message interpolation key is {@code org.klokwrk.lib.validation.constraint.UnLoCodeFormatConstraint.message}.
 * <p/>
 * Useful reference: https://service.unece.org/trade/locode/Service/LocodeColumn.htm - Section "1.2 Column LOCODE".
 * <p/>
 * The two first digits indicates the country in which the place is located. The values used concur with the  ISO 3166 alpha-2 Country Code. In cases where no ISO 3166 country code element is
 * available, e.g. installations in international waters or international cooperation zones, the code element "XZ" will be used.
 * <p/>
 * Next part contains a 3-character code for the location. The 3-character code element for the location will normally comprise three letters. However, where all permutations available for a
 * country have been exhausted, the numerals 2-9 may also be used.
 */
@Documented
@Repeatable(UnLoCodeFormatConstraintList)
@Target([FIELD, METHOD, PARAMETER, TYPE_USE, ANNOTATION_TYPE])
@Retention(RUNTIME)
@Constraint(validatedBy = [])
@interface UnLoCodeFormatConstraint {
  String message() default "{org.klokwrk.lib.validation.constraint.UnLoCodeFormatConstraint.message}"

  Class<?>[] groups() default []
  Class<? extends Payload>[] payload() default []
}

/**
 * Defines several {@link UnLoCodeFormatConstraint} annotations on the same element.
 *
 * @see UnLoCodeFormatConstraint
 */
@Documented
@Target([METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE])
@Retention(RUNTIME)
@interface UnLoCodeFormatConstraintList {
  UnLoCodeFormatConstraint[] value()
}
