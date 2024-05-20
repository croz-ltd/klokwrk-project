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

import si.uom.NonSI
import spock.lang.Specification
import systems.uom.common.USCustomary
import tech.units.indriya.ComparableQuantity
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.unit.Units

import javax.measure.Quantity
import javax.measure.quantity.Length
import javax.measure.quantity.Mass
import javax.measure.quantity.Temperature

class QuantityExtensionSpecification extends Specification {
  void "negative() should work as expected"() {
    given:
    Quantity<Mass> givenMassQuantity = Quantities.getQuantity(10, Units.KILOGRAM)
    Quantity<Mass> givenNegativeMassQuantity = givenMassQuantity.negate()

    when:
    Quantity<Mass> expectedNegativeMassQuantity = -givenMassQuantity

    then:
    expectedNegativeMassQuantity == givenNegativeMassQuantity
    expectedNegativeMassQuantity.unit == givenMassQuantity.unit
    expectedNegativeMassQuantity.value == -givenMassQuantity.value
  }

  void "plus() should work as expected"() {
    given:
    Quantity<Mass> givenMassQuantity1 = Quantities.getQuantity(10, Units.KILOGRAM)
    Quantity<Mass> givenMassQuantity2 = Quantities.getQuantity(15, Units.KILOGRAM)
    Quantity<Mass> givenMassQuantitySum = givenMassQuantity1.add(givenMassQuantity2)

    when:
    Quantity<Mass> expectedMassQuantitySum = givenMassQuantity1 + givenMassQuantity2

    then:
    expectedMassQuantitySum == givenMassQuantitySum
    expectedMassQuantitySum == Quantities.getQuantity(25, Units.KILOGRAM)
  }

  void "toComparable() should work as expected"() {
    given:
    Quantity<Mass> quantity = Quantities.getQuantity(10, Units.KILOGRAM)

    expect:
    quantity.toComparable() instanceof ComparableQuantity
    quantity === quantity.toComparable()
  }

  @SuppressWarnings(["ChangeToOperator", "CodeNarc.ExplicitCallToEqualsMethod"])
  void "toComparable(Unit) should work as expected"() {
    given:
    Quantity<Mass> quantity = Quantities.getQuantity(10, Units.KILOGRAM)

    when:
    ComparableQuantity<Mass> comparableQuantity = quantity.toComparable(Units.GRAM)

    then:
    comparableQuantity.isEquivalentTo(quantity)
    comparableQuantity <=> quantity == 0
    comparableQuantity == quantity // groovy equals operator uses compareTo() for Comparable instances
    !comparableQuantity.equals(quantity)
  }

  void "getG() should work as expected"() {
    given:
    Quantity<Mass> quantity = Quantities.getQuantity(10_000, Units.GRAM)

    expect:
    10_000.g.isEquivalentTo(quantity)
    10_000.g <=> Quantities.getQuantity(10, Units.KILOGRAM) == 0
    10_000.g == quantity
    10_000.g !== Quantities.getQuantity(10_000, Units.GRAM)
  }

  void "getKg() should work as expected"() {
    given:
    Quantity<Mass> quantity = Quantities.getQuantity(10, Units.KILOGRAM)

    expect:
    10.kg.isEquivalentTo(quantity)
    10.kg <=> Quantities.getQuantity(10, Units.KILOGRAM) == 0
    10.kg == quantity
    10.kg !== Quantities.getQuantity(10, Units.KILOGRAM)
  }

  void "getT() should work as expected"() {
    given:
    Quantity<Mass> quantity = Quantities.getQuantity(1, NonSI.TONNE)

    expect:
    1.t.isEquivalentTo(quantity)
    1.t <=> Quantities.getQuantity(1, NonSI.TONNE) == 0
    1.t == quantity
    1.t !== Quantities.getQuantity(1, NonSI.TONNE)
  }

  void "getLb() should work as expected"() {
    given:
    Quantity<Mass> quantity = Quantities.getQuantity(100, USCustomary.POUND)

    expect:
    100.lb.isEquivalentTo(quantity)
    100.lb <=> Quantities.getQuantity(100, USCustomary.POUND) == 0
    100.lb == quantity
    100.lb !== Quantities.getQuantity(100, USCustomary.POUND)
  }

  void "getDegC() should work as expected"() {
    given:
    Quantity<Temperature> quantity = Quantities.getQuantity(10, Units.CELSIUS)

    expect:
    10.degC.isEquivalentTo(quantity)
    10.degC <=> Quantities.getQuantity(10, Units.CELSIUS) == 0
    10.degC == quantity
    10.degC !== Quantities.getQuantity(10, Units.CELSIUS)
  }

  void "getDegF() should work as expected"() {
    given:
    Quantity<Temperature> quantity = Quantities.getQuantity(50, USCustomary.FAHRENHEIT)

    expect:
    50.degF.isEquivalentTo(quantity)
    50.degF <=> Quantities.getQuantity(50, USCustomary.FAHRENHEIT) == 0
    50.degF == quantity
    50.degF !== Quantities.getQuantity(50, USCustomary.FAHRENHEIT)
  }

  void "getM() should work as expected"() {
    given:
    Quantity<Length> quantity = Quantities.getQuantity(100, Units.METRE)

    expect:
    100.m.isEquivalentTo(quantity)
    100.m <=> Quantities.getQuantity(100, Units.METRE) == 0
    100.m == quantity
    100.m !== Quantities.getQuantity(100, Units.METRE)
  }
}
