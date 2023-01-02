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
 * Validates the unit of the provided quantity (instance of {@code javax.measure.Quantity}).
 * <p/>
 * Example usage:
 * <pre>
 * class SomeClassWithQuantities {
 *   // The unit of validated quantity must be compatible with a unit with symbol kg.
 *   &#64;QuantityUnitConstraint(unitSymbol = "kg")
 *   Quantity<Mass> someWeightQuantity
 *
 *   &#64;QuantityUnitConstraint(unitSymbol = "kg")
 *   Quantity someUntypedWeightQuantity
 *
 *   // The unit of validated quantity must be equal with a unit with symbol kg.
 *   &#64;QuantityUnitConstraint(unitSymbol = "kg", acceptOnlyExactUnitSymbol = true)
 *   Quantity<Mass> someOtherWeight
 *
 *   // The unit of validated quantity must be compatible with a unit with symbol kg. If not,
 *   // failed validation message will contain compatibleUnitSymbolsForMessage elements as a
 *   // suggestion for a user.
 *   &#64;QuantityUnitConstraint(unitSymbols = "kg", compatibleUnitSymbolsForMessage = ["kg", "g"])
 *   Quantity someOtherUntypedWeightQuantity
 * }
 * </pre>
 * Unit symbols corresponding to a particular unit are defined by units of measurement JSR-385 reference implementation and their extensions. Look at the
 * <a href="https://github.com/unitsofmeasurement/indriya/blob/master/src/main/java/tech/units/indriya/unit/Units.java">tech.units.indriya.unit.Units</a> class for a base set of units and their
 * symbols (<a href="https://github.com/unitsofmeasurement/indriya">https://github.com/unitsofmeasurement/indriya</a>).
 * <p/>
 * Parameter {@code unitSymbol} must be specified. By default, it defines a <b>compatible</b> unit of a validated quantity. If the value of {@code unitSymbol} not configured or it is not recognized
 * by the underlying JSR-385 implementation, {@code AssertionError} is thrown during validator initialization.
 * <p/>
 * For example, suppose we expect a mass quantity and a {@code unitSymbol} is set to {@code kg}. In that case, we will accept a validated quantity with units in kilograms (unit symbol {@code kg}),
 * grams (unit symbol {@code g}), or any other unit supported and used for mass quantities by our JSR-386 implementation.
 * <p/>
 * By configuring {@code acceptOnlyExactUnitSymbol} to {@code true} (default is {@code false}), we accept only quantities with units <b>equal</b> to kilograms. Compatible units are not accepted in
 * that case.
 * <p/>
 * Parameter {@code compatibleUnitSymbolsForMessage} is optional and is used only for creating failed validation messages. When configured, the failed validation message will contain all listed unit
 * symbols as a convenience for a user.
 * <p/>
 * In default validator implementation, default message interpolation key is {@code org.klokwrk.lib.validation.constraint.uom.QuantityUnitConstraint.invalidUnitSymbolMessage}.
 * <p/>
 * For custom message interpolation (when {@code message} annotation param is defined), default validator implementation exposes {@code expectedUnitSymbol}, {@code providedUnitSymbol}, and
 * {@code compatibleUnitSymbols} expressions.
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
  static final String INVALID_UNIT_SYMBOL_MESSAGE_KEY = "org.klokwrk.lib.validation.constraint.uom.QuantityUnitConstraint.invalidUnitSymbolMessage"

  String unitSymbol() default ""
  boolean acceptOnlyExactUnitSymbol() default false
  String[] compatibleUnitSymbolsForMessage() default []

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
