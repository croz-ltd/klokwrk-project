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
package org.klokwrk.lib.jackson.databind.ser

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import spock.lang.Specification
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.unit.Units

import javax.measure.MetricPrefix
import javax.measure.Quantity
import javax.measure.quantity.Mass

class UomQuantitySerializerSpecification extends Specification {
  static class MyBeanTypedQuantity {
    String name
    Quantity<Mass> weight
  }

  static class MyBeanRawQuantity {
    String otherName
    Quantity length
  }

  ObjectMapper objectMapper

  void setup() {
    SimpleModule simpleModule = new SimpleModule()
    simpleModule.addSerializer(Quantity, new UomQuantitySerializer())

    ObjectMapper objectMapper = new ObjectMapper()
    objectMapper.registerModule(simpleModule)

    this.objectMapper = objectMapper
  }

  void "should serialize mass quantity given with base units of kilograms"() {
    given:
    MyBeanTypedQuantity myBeanTypedQuantity = new MyBeanTypedQuantity(name: "someName", weight: Quantities.getQuantity(quantityValueParam, Units.KILOGRAM))

    when:
    String serializedString = objectMapper.writeValueAsString(myBeanTypedQuantity)

    then:
    serializedString.contains(/"weight":{"value":$quantityValueOutputParam,"unitSymbol":"kg"}/)

    where:
    quantityValueParam | quantityValueOutputParam
    1234               | 1234
    1234.0             | 1234
    1234.4             | 1234.4
    1234.40            | 1234.40
    1234.40000         | 1234.40000
    0                  | 0
    0.0                | 0
    0.01               | 0.01
    -1                 | -1
    -1.0               | -1
    -1.01              | -1.01
  }

  void "should serialize quantity with derived units"() {
    given:
    //noinspection GroovyAssignabilityCheck
    MyBeanTypedQuantity myBeanTypedQuantity = new MyBeanTypedQuantity(name: "someName", weight: Quantities.getQuantity(quantityValueParam, quantityUnitParam))

    when:
    String serializedString = objectMapper.writeValueAsString(myBeanTypedQuantity)

    then:
    serializedString.contains(/"weight":{"value":$quantityValueParam,"unitSymbol":"$unitSymbolParam"}/)

    where:
    quantityValueParam | quantityUnitParam                 | unitSymbolParam
    1234               | Units.GRAM                        | "g"
    1234.01            | Units.GRAM                        | "g"
    1234               | MetricPrefix.MILLI(Units.GRAM)    | "mg"
    1234.01            | MetricPrefix.MILLI(Units.GRAM)    | "mg"
    1234               | MetricPrefix.KILO(Units.KILOGRAM) | "kkg"
    1234.01            | MetricPrefix.KILO(Units.KILOGRAM) | "kkg"
    10                 | Units.CELSIUS                     | "°C"
    10.5               | Units.CELSIUS                     | "°C"
  }

  void "should serialize raw quantity"() {
    given:
    MyBeanRawQuantity myBeanRawQuantity = new MyBeanRawQuantity(otherName: "someName", length: Quantities.getQuantity(quantityValueParam, Units.METRE))

    when:
    String serializedString = objectMapper.writeValueAsString(myBeanRawQuantity)

    then:
    serializedString.contains(/"length":{"value":$quantityValueOutputParam,"unitSymbol":"m"}/)

    where:
    quantityValueParam | quantityValueOutputParam
    1234               | 1234
    1234.0             | 1234
    1234.4             | 1234.4
    1234.40            | 1234.40
    1234.40000         | 1234.40000
    0                  | 0
    0.0                | 0
    0.01               | 0.01
    -1                 | -1
    -1.0               | -1
    -1.01              | -1.01
  }
}
