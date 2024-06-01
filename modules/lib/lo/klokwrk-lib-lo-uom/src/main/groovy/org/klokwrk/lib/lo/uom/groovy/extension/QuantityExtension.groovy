/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2024 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.lib.lo.uom.groovy.extension

import groovy.transform.CompileStatic
import org.klokwrk.lib.lo.uom.format.KwrkQuantityFormat
import si.uom.NonSI
import systems.uom.common.USCustomary
import tech.units.indriya.ComparableQuantity
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.unit.Units

import javax.measure.Quantity
import javax.measure.Unit
import javax.measure.quantity.Length
import javax.measure.quantity.Mass
import javax.measure.quantity.Temperature

/**
 * Groovy extension for UOM <code>Quantity</code> containing convenience methods for easier and expressive usage.
 */
@SuppressWarnings("unused")
@CompileStatic
class QuantityExtension {
  /**
   * Convenient conversion of quantity into a string based on {@link KwrkQuantityFormat}.
   * <p/>
   * Instead of
   * <pre>
   * KwrkQuantityFormat.instance.format(quantity)
   * </pre>
   * one can simply use
   * <pre>
   * quantity.format()
   * </pre>
   */
  static String format(Quantity quantity) {
    String formattedString = KwrkQuantityFormat.instance.format(quantity)
    return formattedString
  }

  /**
   * Convenient negation of a quantity.
   * <p/>
   * Instead of
   * <pre>
   * quantity.negate()
   * </pre>
   * one can simply use
   * <pre>
   * -quantity
   * </pre>
   */
  static Quantity negative(Quantity quantity) {
    Quantity quantityResult = quantity.negate()
    return quantityResult
  }

  /**
   * Convenient addition for quantities.
   * <p/>
   * Instead of
   * <pre>
   * quantity1.add(quantity2)
   * </pre>
   * one can simply use
   * <pre>
   * quantity1 + quantity2
   * </pre>
   */
  static Quantity plus(Quantity selfQuantity, Quantity quantityToAdd) {
    Quantity sum = selfQuantity.add(quantityToAdd)
    return sum
  }

  /**
   * Convenient cast of {@link Quantity} to {@link ComparableQuantity}.
   * <p/>
   * Instead of
   * <pre>
   * quantity as ComparableQuantity
   * </pre>
   * one can use
   * <pre>
   * quantity.toComparable()
   * </pre>
   */
  static ComparableQuantity toComparable(Quantity quantity) {
    // concrete instance of Quantity is always ComparableQuantity with indriya implementation
    ComparableQuantity myQuantity = quantity as ComparableQuantity
    return myQuantity
  }

  /**
   * Convenient cast of {@link Quantity} to {@link ComparableQuantity} together with unit conversion.
   * <p/>
   * Instead of
   * <pre>
   * quantity.to(unit) as ComparableQuantity
   * </pre>
   * one can use
   * <pre>
   * quantity.toComparable(unit)
   * </pre>
   */
  static ComparableQuantity toComparable(Quantity quantity, Unit unit) {
    ComparableQuantity myQuantity = quantity.to(unit) as ComparableQuantity
    return myQuantity
  }

  /**
   * Convenient creation of {@code Quantity<Mass>} with gram units.
   * <p/>
   * Instead of
   * <pre>
   * Quantities.getQuantity(10_000, Units.GRAM)
   * </pre>
   * one can simply use
   * <pre>
   * 10_000.g
   * </pre>
   * Returned quantity is an instance of {@code ComparableQuantity<Mass>}.
   */
  static ComparableQuantity<Mass> getG(Number num) {
    ComparableQuantity<Mass> quantity = Quantities.getQuantity(num, Units.GRAM)
    return quantity
  }

  /**
   * Convenient creation of {@code Quantity<Mass>} with kilogram units.
   * <p/>
   * Instead of
   * <pre>
   * Quantities.getQuantity(10, Units.KILOGRAM)
   * </pre>
   * one can simply use
   * <pre>
   * 10.kg
   * </pre>
   * Returned quantity is an instance of {@code ComparableQuantity<Mass>}.
   */
  static ComparableQuantity<Mass> getKg(Number num) {
    ComparableQuantity<Mass> quantity = Quantities.getQuantity(num, Units.KILOGRAM)
    return quantity
  }

  /**
   * Convenient creation of {@code Quantity<Mass>} with tonne units.
   * <p/>
   * Instead of
   * <pre>
   * Quantities.getQuantity(10, NonSI.TONNE)
   * </pre>
   * one can simply use
   * <pre>
   * 10.t
   * </pre>
   * Returned quantity is an instance of {@code ComparableQuantity<Mass>}.
   */
  static ComparableQuantity<Mass> getT(Number num) {
    ComparableQuantity<Mass> quantity = Quantities.getQuantity(num, NonSI.TONNE)
    return quantity
  }

  /**
   * Convenient creation of {@code Quantity<Mass>} with pound units.
   * <p/>
   * Instead of
   * <pre>
   * Quantities.getQuantity(100, USCustomary.POUND)
   * </pre>
   * one can simply use
   * <pre>
   * 100.lb
   * </pre>
   * Returned quantity is an instance of {@code ComparableQuantity<Mass>}.
   */
  static ComparableQuantity<Mass> getLb(Number num) {
    ComparableQuantity<Mass> quantity = Quantities.getQuantity(num, USCustomary.POUND)
    return quantity
  }

  /**
   * Convenient creation of {@code Quantity<Temperature>} with celsius units.
   * <p/>
   * Instead of
   * <pre>
   * Quantities.getQuantity(10, Units.CELSIUS)
   * </pre>
   * one can simply use
   * <pre>
   * 10.degC
   * </pre>
   * {@code degC} stands for celsius degree.
   * <p/>
   * Returned quantity is an instance of {@code ComparableQuantity<Mass>}.
   */
  static ComparableQuantity<Temperature> getDegC(Number num) {
    ComparableQuantity<Temperature> quantity = Quantities.getQuantity(num, Units.CELSIUS)
    return quantity
  }

  /**
   * Convenient creation of {@code Quantity<Temperature>} with fahrenheit units.
   * <p/>
   * Instead of
   * <pre>
   * Quantities.getQuantity(50, USCustomary.FAHRENHEIT)
   * </pre>
   * one can simply use
   * <pre>
   * 50.degF
   * </pre>
   * {@code degF} stands for fahrenheit degree.
   * <p/>
   * Returned quantity is an instance of {@code ComparableQuantity<Mass>}.
   */
  static ComparableQuantity<Temperature> getDegF(Number num) {
    ComparableQuantity<Temperature> quantity = Quantities.getQuantity(num, USCustomary.FAHRENHEIT)
    return quantity
  }

  /**
   * Convenient creation of {@code Quantity<Length>} with meter units.
   * <p/>
   * Instead of
   * <pre>
   * Quantities.getQuantity(100, Units.METRE)
   * </pre>
   * one can simply use
   * <pre>
   * 100.m
   * </pre>
   * Returned quantity is an instance of {@code ComparableQuantity<Mass>}.
   */
  static ComparableQuantity<Length> getM(Number num) {
    ComparableQuantity<Length> quantity = Quantities.getQuantity(num, Units.METRE)
    return quantity
  }
}
