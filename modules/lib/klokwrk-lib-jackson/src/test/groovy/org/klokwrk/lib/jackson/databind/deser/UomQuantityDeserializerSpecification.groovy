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
package org.klokwrk.lib.jackson.databind.deser

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import spock.lang.Specification
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.unit.Units

import javax.measure.Quantity
import javax.measure.format.MeasurementParseException
import javax.measure.quantity.Mass
import javax.measure.quantity.Temperature

class UomQuantityDeserializerSpecification extends Specification {
  static class MyBeanTypedQuantity {
    String name
    Quantity<Mass> weight
  }

  static class MyBeanRawQuantity {
    String otherName
    Quantity length
  }

  static class MyBeanWithTemperatureQuantity {
    Quantity<Temperature> temperature
  }

  ObjectMapper objectMapper

  void setup() {
    SimpleModule simpleModule = new SimpleModule()
    simpleModule.addDeserializer(Quantity, new UomQuantityDeserializer())

    ObjectMapper objectMapper = new ObjectMapper()
    objectMapper.registerModule(simpleModule)

    this.objectMapper = objectMapper
  }

  void "should work for typed quantity of mass"() {
    given:
    String stringToDeserialize = """
      {
        "name": "someName",
        "weight": {
          "value": $valueParam,
          "unitSymbol": "$unitSymbolParam"
        }
      }
      """

    when:
    MyBeanTypedQuantity deserializedMyBean = objectMapper.readValue(stringToDeserialize, MyBeanTypedQuantity)

    then:
    deserializedMyBean.name == "someName"
    deserializedMyBean.weight == Quantities.getQuantity(1234, Units.KILOGRAM)

    where:
    valueParam | unitSymbolParam
    1_234      | "kg"
    1_234      | " kg"
    1_234      | "kg "
    1_234      | " kg "
    1_234_000  | "g"
  }

  void "should work for typed quantity of temperature"() {
    given:
    String absoluteTemperatureStringToDeserialize = """
      {
        "temperature": {
          "value": $valueParam,
          "unitSymbol": $unitSymbolParam
        }
      }
      """

    when:
    MyBeanWithTemperatureQuantity temperatureQuantity = objectMapper.readValue(absoluteTemperatureStringToDeserialize, MyBeanWithTemperatureQuantity)

    then:
    temperatureQuantity.temperature == Quantities.getQuantity(valueParam, Units.CELSIUS)

    where:
    valueParam | unitSymbolParam
    10         | /"°C"/
    10.5       | /"°C"/
    10         | /"\u00b0C"/
    10.5       | /"\u00b0C"/
    10         | /"℃"/
    10.5       | /"℃"/
    10         | /"\u2103"/
    10.5       | /"\u2103"/
  }

  void "should work for raw quantity"() {
    given:
    String stringToDeserialize = """
      {
        "otherName": "someName",
        "length": {
          "value": $quantityValueParam,
          "unitSymbol": "$quantityUnitParam"
        }
      }
      """

    when:
    MyBeanRawQuantity deserializedMyBean = objectMapper.readValue(stringToDeserialize, MyBeanRawQuantity)

    then:
    deserializedMyBean.otherName == "someName"
    deserializedMyBean.length == Quantities.getQuantity(1234, Units.METRE)

    where:
    quantityValueParam | quantityUnitParam
    1_234              | "m"
    1_234              | " m"
    1_234              | "m "
    1_234              | " m "
    123_400            | "cm"
  }

  void "should throw for unexpected json"() {
    given:
    String stringToDeserialize = """
      {
        "name": "someName",
        "weight": $weightParam
      }
      """

    when:
    objectMapper.readValue(stringToDeserialize, MyBeanTypedQuantity)

    then:
    JsonMappingException jsonMappingException = thrown()
    jsonMappingException.message.startsWith("Failed to parse Quantity from '$weightParam'.")
    jsonMappingException.cause instanceof MeasurementParseException

    where:
    weightParam                         | _
    '""'                                | _
    '"   "'                             | _
    "123"                               | _
    "[123,456]"                         | _
    '{"value":null,"unitSymbol":"kg"}'  | _
    '{"value":"","unitSymbol":"kg"}'    | _
    '{"value":"  ","unitSymbol":"kg"}'  | _
    '{"value":[123],"unitSymbol":"kg"}' | _
    '{"value":123,"unitSymbol":null}'   | _
    '{"value":123,"unitSymbol":123}'    | _
    '{"value":123,"unitSymbol":["kg"]}' | _
  }

  void "should throw when dimension is not expected"() {
    given:
    String stringToDeserialize = """
      {
        "name": "someName",
        "weight": {
          "value": 1234,
          "unitSymbol": $unitSymbolParam
        }
      }
      """

    when:
    objectMapper.readValue(stringToDeserialize, MyBeanTypedQuantity)

    then:
    JsonMappingException jsonMappingException = thrown()
    jsonMappingException.message.startsWith("Failed to parse unexpected dimension of a quantity [quantity: 1234")
    jsonMappingException.message.contains("expected dimension: Mass].")
    jsonMappingException.cause instanceof MeasurementParseException

    where:
    unitSymbolParam | _
    /""/            | _
    /"   "/         | _
    /"m"/           | _
  }

  void "should throw for non parsable values"() {
    given:
    String stringToDeserialize = """
      {
        "name": "someName",
        "weight": {
          "value": "abc",
          "unitSymbol": "m"
        }
      }
      """

    when:
    objectMapper.readValue(stringToDeserialize, MyBeanTypedQuantity)

    then:
    JsonMappingException jsonMappingException = thrown()
    jsonMappingException.message.startsWith(/Failed to parse Quantity from '{"value":"abc","unitSymbol":"m"}'./)
    jsonMappingException.cause instanceof MeasurementParseException
  }

  void "should throw for unknown unit"() {
    given:
    String stringToDeserialize = """
      {
        "name": "someName",
        "weight": {
          "value": 1234,
          "unitSymbol": "abc"
        }
      }
      """

    when:
    objectMapper.readValue(stringToDeserialize, MyBeanTypedQuantity)

    then:
    JsonMappingException jsonMappingException = thrown()
    jsonMappingException.message.startsWith(/Failed to parse Quantity from '{"value":1234,"unitSymbol":"abc"}'./)
    jsonMappingException.cause instanceof MeasurementParseException
  }
}
