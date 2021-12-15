package org.klokwrk.lib.jackson.databind.ser

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import spock.lang.Specification
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.unit.Units

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

  void "should serialize typed quantity"() {
    given:
    MyBeanTypedQuantity myBeanTypedQuantity = new MyBeanTypedQuantity(name: "someName", weight: Quantities.getQuantity(quantityValueParam, Units.KILOGRAM))

    when:
    String serializedString = objectMapper.writeValueAsString(myBeanTypedQuantity)

    then:
    serializedString.contains(/"weight":{"value":$quantityValueOutputParam,"unit":"kg"}/)

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

  void "should serialize typed quantity with scale"() {
    given:
    MyBeanTypedQuantity myBeanTypedQuantity = new MyBeanTypedQuantity(name: "someName", weight: Quantities.getQuantity(123, Units.KILOGRAM, Quantity.Scale.RELATIVE))

    when:
    String serializedString = objectMapper.writeValueAsString(myBeanTypedQuantity)

    then:
    serializedString.contains(/"weight":{"value":123,"unit":"kg","scale":"RELATIVE"}/)
  }

  void "should serialize raw quantity"() {
    given:
    MyBeanRawQuantity myBeanRawQuantity = new MyBeanRawQuantity(otherName: "someName", length: Quantities.getQuantity(quantityValueParam, Units.METRE))

    when:
    String serializedString = objectMapper.writeValueAsString(myBeanRawQuantity)

    then:
    serializedString.contains(/"length":{"value":$quantityValueOutputParam,"unit":"m"}/)

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
