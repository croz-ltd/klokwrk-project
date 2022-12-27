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
package org.klokwrk.lib.validation.constraint.uom

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
 * This constraint validates the unit of the annotated quantity (instance of {@code javax.measure.Quantity}).
 * <p/>
 * Example usage:
 * <pre>
 * class SomeClassWithQuantities {
 *   &#64;QuantityUnitConstraint(exactUnitSymbol = "kg")
 *   Quantity<Mass> someWeight
 *
 *   &#64;QuantityUnitConstraint(exactUnitSymbol = "kg")
 *   Quantity someUntypedWeightQuantity
 *
 *   &#64;QuantityUnitConstraint(compatibleUnitSymbols = ["kg", "g"])
 *   Quantity<Mass> someOtherWeight
 *
 *   &#64;QuantityUnitConstraint(compatibleUnitSymbols = ["kg", "g"])
 *   Quantity someOtherUntypedWeightQuantity
 * }
 * </pre>
 * Unit symbols corresponding to a particular unit are defined by units of measurement JSR-385 reference implementation and their extensions. Look at the {@code tech.units.indriya.unit.Units} class
 * for a base set of units and their symbols (<a href="https://github.com/unitsofmeasurement/indriya">https://github.com/unitsofmeasurement/indriya</a>).
 * <p/>
 * Constraint parameter {@code compatibleUnitSymbols} is used when we want to check for compatible units of a quantity. For example, suppose we have a mass quantity. In that case, we can specify its
 * units in kilograms (unit symbol kg), grams (unit symbol g), or any other unit supported and used for mass quantities by our JSR-386 implementation.
 * <p/>
 * Only the first specified unit symbol is used for the compatibility check of units, while others are used just for constraint violation reporting. If the first specified unit symbol is not
 * recognized by JSR-385 implementation, {@code AssertionError} is thrown during validator initialization.
 * <p/>
 * Constraint parameter {@code exactUnitSymbol} is used when we want exact matching of quantity units. In this case, units are checked for equality, not for compatibility. If the specified
 * {@code exactUnitSymbol} is not recognized by JSR-385 implementation, {@code AssertionError} is thrown during validator initialization.
 * <p/>
 * Parameters {@code exactUnitSymbol} and {@code compatibleUnitSymbols} are mutually exclusive. Therefore, only one of them must be specified. If both or none are specified, the validator will throw
 * {@code AssertionError} during validator initialization.
 * <p/>
 * In default validator implementation, default message interpolation keys (when {@code message} annotation param is empty) are
 * {@code org.klokwrk.lib.validation.constraint.uom.QuantityUnitConstraint.invalidExactUnitSymbolMessage} (when using {@code exactUnitSymbol} parameter) and
 * {@code org.klokwrk.lib.validation.constraint.uom.QuantityUnitConstraint.invalidCompatibleUnitSymbolMessage} (when using {@code compatibleUnitSymbols} parameter).
 * <p/>
 * For custom message interpolation (when {@code message} annotation param is configured), default validator implementation exposes {@code specifiedExactUnitSymbol} and
 * {@code specifiedCompatibleUnitSymbols} expressions.
 * <p/>
 * When custom annotation {@code message} parameter value is specified, it can be either a reference to the resource bundle key (must be enclosed in curly braces '<code>{}</code>'), or a hardcoded
 * message. In resource bundle properties files and in the hardcoded message, exposed interpolation expressions must be enclosed in curly braces starting with a dollar sign '<code>${}</code>'.
 */
@Documented
@Repeatable(QuantityUnitConstraintList)
@Target([FIELD, METHOD, PARAMETER, TYPE_USE, ANNOTATION_TYPE])
@Retention(RUNTIME)
@Constraint(validatedBy = [])
@interface QuantityUnitConstraint {
  static final String INVALID_EXACT_UNIT_SYMBOL_MESSAGE_KEY = "org.klokwrk.lib.validation.constraint.uom.QuantityUnitConstraint.invalidExactUnitSymbolMessage"
  static final String INVALID_COMPATIBLE_UNIT_SYMBOL_MESSAGE_KEY = "org.klokwrk.lib.validation.constraint.uom.QuantityUnitConstraint.invalidCompatibleUnitSymbolMessage"

  String exactUnitSymbol() default ""
  String[] compatibleUnitSymbols() default []

  String message() default ""
  Class<?>[] groups() default []
  Class<? extends Payload>[] payload() default []
}

@Documented
@Target([METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE])
@Retention(RUNTIME)
@interface QuantityUnitConstraintList {
  QuantityUnitConstraint[] value()
}
