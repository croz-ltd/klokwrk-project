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
package org.klokwrk.lib.hi.validation.springboot

import org.klokwrk.lib.validation.constraint.TrimmedStringConstraint
import org.klokwrk.lib.validation.constraint.uom.QuantityUnitConstraint
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

import jakarta.validation.ConstraintViolationException
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import javax.measure.Quantity
import javax.measure.quantity.Mass

@SpringBootTest
class ValidationServiceDefaultSetupSpecification extends Specification {
  @Autowired
  ValidationService validationService

  static class TestObject {
    @TrimmedStringConstraint
    @Size(min = 1, max = 15)
    @NotNull
    String stringProperty

    @QuantityUnitConstraint(unitSymbol = "kg")
    Quantity<Mass> quantityOfMass
  }

  void "default configuration should be applied"() {
    expect:
    validationService.enabled
    validationService.messageSourceBaseNames == ["klokwrkValidationConstraintMessages"] as String[]
    validationService.validatorImplementationPackagesToScan == ["org.klokwrk.lib.lo.validation.validator.."] as String[]
  }

  void "should not throw for valid object"() {
    given:
    TestObject testObject = new TestObject(stringProperty: "bla", quantityOfMass: 10.kg)

    when:
    validationService.validate(testObject)

    then:
    true
  }

  void "should throw for invalid object"() {
    given:
    TestObject testObject = new TestObject(stringProperty: stringPropertyParam, quantityOfMass: quantityOfMassParam as Quantity<Mass>)

    when:
    validationService.validate(testObject)

    then:
    thrown(ConstraintViolationException)

    where:
    stringPropertyParam | quantityOfMassParam
    null                | null
    ""                  | null
    "bla "              | null
    "bla"               | 10.m
  }
}
