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
package org.klokwrk.lib.lo.validation.constraint.uom

import jakarta.validation.Constraint
import jakarta.validation.Payload
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
 * Validates the quantity (instance of {@code javax.measure.Quantity}) against specified quantity range.
 * <p/>
 * Example usage:
 * <pre>
 * class SomeClassWithQuantities {
 *   // Allowed minimum is 10 kilograms inclusively and allowed maximum is 100 kg inclusively.
 *   // Provided quantity must be greater than or equal to 10 kilograms and less than or equal to 100 kg.
 *   // The validated quantity must have a unit compatible with kilograms.
 *   &#64;QuantityRangeConstraint(minQuantity = "10 kg", maxQuantity = "100 kg")
 *   Quantity<Mass> quantityMinAndMaxInclusive
 *
 *   // Allowed minimum is 10 kilograms exclusively and allowed maximum is 100 kg exclusively.
 *   // Provided quantity must be strictly greater than 10 kilograms and strictly less than 100 kg.
 *   // The validated quantity must have a unit compatible with kilograms.
 *   &#64;QuantityRangeConstraint(minQuantity = "10 kg", minInclusive = false, maxQuantity = "100 kg", maxInclusive = false)
 *   Quantity<Mass> quantityMinExclusiveAndMaxExclusive
 *
 *   // Allowed minimum is 10 kilograms inclusively and allowed maximum is 100 kg exclusively.
 *   // The validated quantity must have a unit strictly equal to kilograms.
 *   &#64;QuantityRangeConstraint(minQuantity = "10 kg", maxQuantity = "100 kg", maxInclusive = false, acceptOnlyEqualUnit = true)
 *   Quantity someUntypedWeightQuantity
 * </pre>
 * Unit symbols corresponding to a particular unit are defined by units of measurement JSR-385 reference implementation and their extensions. Look at the
 * <a href="https://github.com/unitsofmeasurement/indriya/blob/master/src/main/java/tech/units/indriya/unit/Units.java">tech.units.indriya.unit.Units</a> class for a base set of units and their
 * symbols (<a href="https://github.com/unitsofmeasurement/indriya">https://github.com/unitsofmeasurement/indriya</a>).
 * <p/>
 * Parameters {@code minQuantity} and {@code maxQuantity} must be specified. By default, the units of {@code minQuantity} and {@code maxQuantity} must be mutually compatible. If they are not mutually
 * compatible {@code AssertionError} is thrown during validator initialization.
 * <p/>
 * Parameter {@code acceptOnlyEqualUnit} is optional (default value is {@code false}). It cannot be specified individually for minimum or maximum. Instead, both boundaries must be set with equal
 * units when {@code acceptOnlyEqualUnit} is {@code true}. Otherwise, {@code AssertionError} will be thrown during validator initialization.
 * <p/>
 * The default value of the {@code minInclusive} and {@code maxInclusive} parameters is {@code true}. This means the validated quantity can be greater than or equal to the configured
 * {@code minQuantity} and less than or equal to {@code maxQuantity}. On the other hand, if {@code minInclusive} is set to {@code false}, the validated quantity has to be strictly greater than
 * {@code minQuantity}. Similarly, when {@code maxInclusive} is set to {@code false}, the validated quantity has to be strictly less than {@code maxQuantity}.
 * <p/>
 * For message interpolation, the default validator provides six types of message templates with corresponding resource bundle keys:
 * <ul>
 * <li>
 *   INVALID_UNIT_EQUAL: {@code org.klokwrk.lib.lo.validation.constraint.uom.QuantityRangeConstraint.invalidUnitEqualMessage}
 *   <br/>
 *   Used when equal unit matching is required but not satisfied.
 * </li>
 * <li>
 *   INVALID_UNIT_COMPATIBLE: {@code org.klokwrk.lib.lo.validation.constraint.uom.QuantityRangeConstraint.invalidUnitCompatibleMessage}
 *   <br/>
 *   Used when compatible unit matching is required but not satisfied.
 * </li>
 * <li>
 *   INVALID_QUANTITY_MIN_INCLUSIVE_MAX_INCLUSIVE: {@code org.klokwrk.lib.lo.validation.constraint.uom.QuantityRangeConstraint.invalidQuantityMinInclusiveMaxInclusiveMessage}
 *   <br/>
 *   Used when the inclusive minimum and inclusive maximum are required but not satisfied.
 * </li>
 * <li>
 *   INVALID_QUANTITY_MIN_INCLUSIVE_MAX_EXCLUSIVE: {@code org.klokwrk.lib.lo.validation.constraint.uom.QuantityRangeConstraint.invalidQuantityMinInclusiveMaxExclusiveMessage}
 *   <br/>
 *   Used when the inclusive minimum and exclusive maximum are required but not satisfied.
 * </li>
 * <li>
 *   INVALID_QUANTITY_MIN_EXCLUSIVE_MAX_INCLUSIVE: {@code org.klokwrk.lib.lo.validation.constraint.uom.QuantityRangeConstraint.invalidQuantityMinExclusiveMaxInclusiveMessage}
 *   <br/>
 *   Used when the exclusive minimum and inclusive maximum are required but not satisfied.
 * </li>
 * <li>
 *   INVALID_QUANTITY_MIN_EXCLUSIVE_MAX_EXCLUSIVE: {@code org.klokwrk.lib.lo.validation.constraint.uom.QuantityRangeConstraint.invalidQuantityMinExclusiveMaxExclusiveMessage}
 *   <br/>
 *   Used when the exclusive minimum and exclusive maximum are required but not satisfied.
 * </li>
 * </ul>
 * For all message templates (including custom one provided via annotation's {@code message} parameter), six expressions are provided:
 * <ul>
 * <li>{@code expectedMinQuantity} - the value of the annotation's {@code minQuantity} parameter</li>
 * <li>{@code expectedMinQuantityUnit} - a string representation for a unit of the {@code minQuantity}</li>
 * <li>{@code expectedMaxQuantity} - the value of the annotation's {@code maxQuantity} parameter</li>
 * <li>{@code expectedMaxQuantityUnit} - a string representation for a unit of the {@code maxQuantity}</li>
 * <li>{@code providedQuantity} - a string representation of the validated quantity</li>
 * <li>{@code providedQuantityUnit} - a string representation for a unit of the validated quantity</li>
 * </ul>
 * <p/>
 * When the custom annotation's {@code message} parameter value is specified, it can be either a reference to the resource bundle key (must be enclosed in curly braces '<code>{}</code>') or a
 * hardcoded message. In resource bundle properties files and in the hardcoded message, exposed interpolation expressions must be enclosed in curly braces starting with a dollar sign
 * '<code>${}</code>'.
 *
 * @see QuantityMinConstraint
 * @see QuantityMaxConstraint
 * @see QuantityUnitConstraint
 */
@Documented
@Repeatable(QuantityRangeConstraintList)
@Target([FIELD, METHOD, PARAMETER, TYPE_USE, ANNOTATION_TYPE])
@Retention(RUNTIME)
@Constraint(validatedBy = [])
@interface QuantityRangeConstraint {
  static final String INVALID_UNIT_EQUAL_KEY = "org.klokwrk.lib.lo.validation.constraint.uom.QuantityRangeConstraint.invalidUnitEqualMessage"
  static final String INVALID_UNIT_COMPATIBLE_KEY = "org.klokwrk.lib.lo.validation.constraint.uom.QuantityRangeConstraint.invalidUnitCompatibleMessage"

  static final String INVALID_QUANTITY_MIN_INCLUSIVE_MAX_INCLUSIVE_KEY = "org.klokwrk.lib.lo.validation.constraint.uom.QuantityRangeConstraint.invalidQuantityMinInclusiveMaxInclusiveMessage"
  static final String INVALID_QUANTITY_MIN_INCLUSIVE_MAX_EXCLUSIVE_KEY = "org.klokwrk.lib.lo.validation.constraint.uom.QuantityRangeConstraint.invalidQuantityMinInclusiveMaxExclusiveMessage"
  static final String INVALID_QUANTITY_MIN_EXCLUSIVE_MAX_INCLUSIVE_KEY = "org.klokwrk.lib.lo.validation.constraint.uom.QuantityRangeConstraint.invalidQuantityMinExclusiveMaxInclusiveMessage"
  static final String INVALID_QUANTITY_MIN_EXCLUSIVE_MAX_EXCLUSIVE_KEY = "org.klokwrk.lib.lo.validation.constraint.uom.QuantityRangeConstraint.invalidQuantityMinExclusiveMaxExclusiveMessage"

  String minQuantity() default ""
  boolean minInclusive() default true

  String maxQuantity() default ""
  boolean maxInclusive() default true

  boolean acceptOnlyEqualUnit() default false

  String message() default ""
  Class<?>[] groups() default []
  Class<? extends Payload>[] payload() default []
}

@Documented
@Target([METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE])
@Retention(RUNTIME)
@interface QuantityRangeConstraintList {
  QuantityRangeConstraint[] value()
}
