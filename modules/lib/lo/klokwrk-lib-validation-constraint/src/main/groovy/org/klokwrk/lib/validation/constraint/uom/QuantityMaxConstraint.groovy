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
 * Validates the allowed maximum of the annotated quantity (instance of {@code javax.measure.Quantity}).
 * <p/>
 * Example usage:
 * <pre>
 * class SomeClassWithQuantities {
 *   // Allowed maximum is 200.5 kilograms inclusively.
 *   // Provided quantity must be greater or equal to 200.5 kilograms.
 *   // The validated quantity must have a unit compatible with kilograms.
 *   &#64;QuantityMaxConstraint(maxQuantity = "200.5 kg")
 *   Quantity<Mass> someWeightQuantity
 *
 *   // Allowed maximum is 200.5 kilograms exclusively.
 *   // Provided quantity must be strictly less than 200.5 kilograms.
 *   // The validated quantity must have a unit compatible with kilograms.
 *   &#64;QuantityMaxConstraint(maxQuantity = "200.5 kg", inclusive = false)
 *   Quantity<Mass> someOtherWeight
 *
 *   // Allowed maximum is 100 kilograms inclusively.
 *   // The validated quantity must have a unit strictly equal to kilograms.
 *   &#64;QuantityMaxConstraint(maxQuantity = "5 kg", acceptOnlyEqualUnit = true)
 *   Quantity someUntypedWeightQuantity
 * }
 * </pre>
 * Unit symbols corresponding to a particular unit are defined by units of measurement JSR-385 reference implementation and their extensions. Look at the
 * <a href="https://github.com/unitsofmeasurement/indriya/blob/master/src/main/java/tech/units/indriya/unit/Units.java">tech.units.indriya.unit.Units</a> class for a base set of units and their
 * symbols (<a href="https://github.com/unitsofmeasurement/indriya">https://github.com/unitsofmeasurement/indriya</a>).
 * <p/>
 * Parameter {@code maxQuantity} must be specified. By default, the unit of a validated quantity has to be <b>compatible</b> with the unit of a {@code maxQuantity}. If the value of
 * {@code maxQuantity} is not configured or the underlying JSR-385 implementation does not recognize it, {@code AssertionError} is thrown during validator initialization.
 * <p/>
 * The default value of the {@code inclusive} parameter is {@code true}. This means the validated quantity can be less than or equal to the configured {@code maxQuantity}. If {@code inclusive} is
 * set to {@code false}, the validated quantity has to be strictly less than {@code maxQuantity}.
 * <p/>
 * The default value of {@code acceptOnlyEqualUnit} parameter is {@code false}. This means the unit of validated quantity has to be compatible with the unit of configured {@code maxQuantity}. If
 * {@code acceptOnlyEqualUnit} is set to {@code true}, the unit of validated quantity hat to be equal to the unit of {@code maxQuantity}.
 * <p/>
 * For message interpolation, the default validator provides four types of message templates with corresponding resource bundle keys:
 * <ul>
 * <li>
 *   INVALID_UNIT_EQUAL_UNIT: {@code org.klokwrk.lib.validation.constraint.uom.QuantityMaxConstraint.invalidUnitEqualMessage}
 *   <br/>
 *   Used when equal unit matching is required but not satisfied.
 * </li>
 * <li>
 *   INVALID_UNIT_COMPATIBLE_UNIT: {@code org.klokwrk.lib.validation.constraint.uom.QuantityMaxConstraint.invalidUnitCompatibleMessage}
 *   <br/>
 *   Used when compatible unit matching is required but not satisfied.
 * </li>
 * <li>
 *   INVALID_QUANTITY_INCLUSIVE: {@code org.klokwrk.lib.validation.constraint.uom.QuantityMaxConstraint.invalidQuantityInclusiveMessage}
 *   <br/>
 *   Used when the inclusive maximum is required but not satisfied.
 * </li>
 * <li>
 *   INVALID_QUANTITY_EXCLUSIVE: {@code org.klokwrk.lib.validation.constraint.uom.QuantityMaxConstraint.invalidQuantityExclusiveMessage}
 *   <br/>
 *   Used when the exclusive maximum is required but not satisfied.
 * </li>
 * </ul>
 * For all message templates (including custom one provided via annotation's {@code message} parameter), four expressions are provided:
 * <ul>
 * <li>{@code expectedMaxQuantity} - the value of the annotation's {@code maxQuantity} parameter</li>
 * <li>{@code expectedMaxQuantityUnit} - a string representation for a unit of the {@code maxQuantity}</li>
 * <li>{@code providedQuantity} - a string representation of the validated quantity</li>
 * <li>{@code providedQuantityUnit} - a string representation for a unit of the validated quantity</li>
 * </ul>
 * <p/>
 * When the custom annotation's {@code message} parameter value is specified, it can be either a reference to the resource bundle key (must be enclosed in curly braces '<code>{}</code>') or a
 * hardcoded message. In resource bundle properties files and in the hardcoded message, exposed interpolation expressions must be enclosed in curly braces starting with a dollar sign
 * '<code>${}</code>'.
 */
@Documented
@Repeatable(QuantityMaxConstraintList)
@Target([FIELD, METHOD, PARAMETER, TYPE_USE, ANNOTATION_TYPE])
@Retention(RUNTIME)
@Constraint(validatedBy = [])
@interface QuantityMaxConstraint {
  static final String INVALID_UNIT_EQUAL_KEY = "org.klokwrk.lib.validation.constraint.uom.QuantityMaxConstraint.invalidUnitEqualMessage"
  static final String INVALID_UNIT_COMPATIBLE_KEY = "org.klokwrk.lib.validation.constraint.uom.QuantityMaxConstraint.invalidUnitCompatibleMessage"
  static final String INVALID_QUANTITY_INCLUSIVE_KEY = "org.klokwrk.lib.validation.constraint.uom.QuantityMaxConstraint.invalidQuantityInclusiveMessage"
  static final String INVALID_QUANTITY_EXCLUSIVE_KEY = "org.klokwrk.lib.validation.constraint.uom.QuantityMaxConstraint.invalidQuantityExclusiveMessage"

  String maxQuantity() default ""
  boolean inclusive() default true
  boolean acceptOnlyEqualUnit() default false

  String message() default ""
  Class<?>[] groups() default []
  Class<? extends Payload>[] payload() default []
}

@Documented
@Target([METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE])
@Retention(RUNTIME)
@interface QuantityMaxConstraintList {
  QuantityMaxConstraint[] value()
}
