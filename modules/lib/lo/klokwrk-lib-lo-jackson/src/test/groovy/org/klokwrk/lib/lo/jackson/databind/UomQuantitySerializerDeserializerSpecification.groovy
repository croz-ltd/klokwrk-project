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
package org.klokwrk.lib.lo.jackson.databind

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import groovy.transform.EqualsAndHashCode
import org.klokwrk.lib.lo.jackson.databind.deser.UomQuantityDeserializer
import org.klokwrk.lib.lo.jackson.databind.ser.UomQuantitySerializer
import spock.lang.Specification

import javax.measure.Quantity
import javax.measure.quantity.Mass
import javax.measure.quantity.Temperature

class UomQuantitySerializerDeserializerSpecification extends Specification {
  @EqualsAndHashCode
  static class MyBeanTypedQuantity {
    Quantity<Mass> weight
  }

  @EqualsAndHashCode
  static class MyBeanRawQuantity {
    Quantity length
  }

  @EqualsAndHashCode
  static class MyBeanWithTemperatureQuantity {
    Quantity<Temperature> temperature
  }

  ObjectMapper objectMapper

  void setup() {
    SimpleModule simpleModule = new SimpleModule()
    simpleModule.addSerializer(Quantity, new UomQuantitySerializer())
    simpleModule.addDeserializer(Quantity, new UomQuantityDeserializer())

    ObjectMapper objectMapper = new ObjectMapper()
    objectMapper.registerModule(simpleModule)

    this.objectMapper = objectMapper
  }

  void "deserializer should work with serializer output for typed quantity"() {
    given:
    MyBeanTypedQuantity serializedBean = new MyBeanTypedQuantity(weight: 1234.kg)
    String serializedString = objectMapper.writeValueAsString(serializedBean)

    when:
    MyBeanTypedQuantity deserializedBean = objectMapper.readValue(serializedString, MyBeanTypedQuantity)

    then:
    deserializedBean == serializedBean
  }

  void "deserializer should work with serializer output for raw quantity"() {
    given:
    MyBeanRawQuantity serializedBean = new MyBeanRawQuantity(length: 1234.m)
    String serializedString = objectMapper.writeValueAsString(serializedBean)

    when:
    MyBeanRawQuantity deserializedBean = objectMapper.readValue(serializedString, MyBeanRawQuantity)

    then:
    deserializedBean == serializedBean
  }

  void "deserializer should work with serializer output for temperature quantity"() {
    given:
    MyBeanWithTemperatureQuantity serializedBean = new MyBeanWithTemperatureQuantity(temperature: 10.degC)
    String serializedString = objectMapper.writeValueAsString(serializedBean)

    when:
    MyBeanWithTemperatureQuantity deserializedBean = objectMapper.readValue(serializedString, MyBeanWithTemperatureQuantity)

    then:
    deserializedBean == serializedBean
  }

  void "deserializer should work with serializer output of normalized quantity"() {
    given:
    MyBeanTypedQuantity serializedBean = new MyBeanTypedQuantity(weight: 1_234_000.g)
    String serializedString = objectMapper.writeValueAsString(serializedBean)

    when:
    MyBeanTypedQuantity deserializedBean = objectMapper.readValue(serializedString, MyBeanTypedQuantity)

    then:
    deserializedBean == serializedBean
    deserializedBean == new MyBeanTypedQuantity(weight: 1_234.kg)
  }
}
