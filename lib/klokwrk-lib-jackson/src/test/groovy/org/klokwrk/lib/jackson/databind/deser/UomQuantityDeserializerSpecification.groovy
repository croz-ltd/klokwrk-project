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

  void "should work for quantity given as a string - typed quantity"() {
    given:
    String stringToDeserialize = """
      {
        "name": "someName",
        "weight": "$quantityStringParam"
      }
      """

    when:
    MyBeanTypedQuantity deserializedMyBean = objectMapper.readValue(stringToDeserialize, MyBeanTypedQuantity)

    then:
    deserializedMyBean.name == "someName"
    deserializedMyBean.weight == Quantities.getQuantity(1234, Units.KILOGRAM)
    deserializedMyBean.weight.scale == Quantity.Scale.ABSOLUTE

    where:
    quantityStringParam | _
    "1234 kg"           | _
    " 1234 kg"          | _
    "1234 kg "          | _
    " 1234 kg "         | _
    "1234000 g"         | _
  }

  void "should work for quantity given as a string - raw quantity"() {
    given:
    String stringToDeserialize = """
      {
        "otherName": "someName",
        "length": "$quantityStringParam"
      }
      """

    when:
    MyBeanRawQuantity deserializedMyBean = objectMapper.readValue(stringToDeserialize, MyBeanRawQuantity)

    then:
    deserializedMyBean.otherName == "someName"
    deserializedMyBean.length == Quantities.getQuantity(1234, Units.METRE)
    deserializedMyBean.length.scale == Quantity.Scale.ABSOLUTE

    where:
    quantityStringParam | _
    "1234 m"            | _
    " 1234 m"           | _
    "1234 m "           | _
    " 1234 m "          | _
    "123400 cm"         | _
  }

  void "should work for quantity given as an object - typed quantity"() {
    given:
    String stringToDeserialize = """
      {
        "name": "someName",
        "weight": {
          "value": $quantityValueParam,
          "unit": "$quantityUnitParam"
        }
      }
      """

    when:
    MyBeanTypedQuantity deserializedMyBean = objectMapper.readValue(stringToDeserialize, MyBeanTypedQuantity)

    then:
    deserializedMyBean.name == "someName"
    deserializedMyBean.weight == Quantities.getQuantity(1234, Units.KILOGRAM)
    deserializedMyBean.weight.scale == Quantity.Scale.ABSOLUTE

    where:
    quantityValueParam | quantityUnitParam
    1_234              | "kg"
    1_234              | " kg"
    1_234              | "kg "
    1_234              | " kg "
    1_234_000          | "g"
  }

  void "should work for quantity given as an object - typed quantity with scale"() {
    given:
    String stringToDeserialize = """
      {
        "name": "someName",
        "weight": {
          "value": 1234,
          "unit": "kg",
          "scale": $quantityScaleStringParam
        }
      }
      """

    when:
    MyBeanTypedQuantity deserializedMyBean = objectMapper.readValue(stringToDeserialize, MyBeanTypedQuantity)

    then:
    deserializedMyBean.name == "someName"
    deserializedMyBean.weight == Quantities.getQuantity(1234, Units.KILOGRAM)
    deserializedMyBean.weight.scale == quantityScaleParam

    where:
    quantityScaleStringParam | quantityScaleParam
    null                     | Quantity.Scale.ABSOLUTE
    /""/                     | Quantity.Scale.ABSOLUTE
    /"  "/                   | Quantity.Scale.ABSOLUTE
    /"unknown scale"/        | Quantity.Scale.ABSOLUTE
    123                      | Quantity.Scale.ABSOLUTE

    /"absolute"/             | Quantity.Scale.ABSOLUTE
    /"ABSOLUTE"/             | Quantity.Scale.ABSOLUTE
    /"absolute "/            | Quantity.Scale.ABSOLUTE
    /" absolute "/           | Quantity.Scale.ABSOLUTE
    /"abSOLute"/             | Quantity.Scale.ABSOLUTE

    /"relative"/             | Quantity.Scale.RELATIVE
    /"RELATIVE"/             | Quantity.Scale.RELATIVE
    /"relative "/            | Quantity.Scale.RELATIVE
    /" relative "/           | Quantity.Scale.RELATIVE
    /"reLATive"/             | Quantity.Scale.RELATIVE
  }

  void "should work for quantity given as an object - typed quantity with scale and calculation"() {
    given:
    String absoluteTemperatureStringToDeserialize = """
      {
        "temperature": {
          "value": 5,
          "unit": "℃"
        }
      }
      """

    String relativeTemperatureStringToDeserialize = """
      {
        "temperature": {
          "value": 5,
          "unit": "℃",
          "scale": "relative"
        }
      }
    """

    when:
    MyBeanWithTemperatureQuantity absoluteTemperature = objectMapper.readValue(absoluteTemperatureStringToDeserialize, MyBeanWithTemperatureQuantity)
    MyBeanWithTemperatureQuantity relativeTemperature = objectMapper.readValue(relativeTemperatureStringToDeserialize, MyBeanWithTemperatureQuantity)

    then:
    absoluteTemperature.temperature.add(relativeTemperature.temperature) == Quantities.getQuantity(10, Units.CELSIUS)
    absoluteTemperature.temperature.add(absoluteTemperature.temperature) == Quantities.getQuantity(283.15, Units.CELSIUS)
  }

  void "should work for temperature quantity with all supported Celsius unit encodings"() {
    given:
    String temperatureStringToDeserialize = """
      {
        "temperature": {
          "value": 5,
          "unit": $unitStringParam
        }
      }
      """

    when:
    MyBeanWithTemperatureQuantity temperatureQuantity = objectMapper.readValue(temperatureStringToDeserialize, MyBeanWithTemperatureQuantity)

    then:
    temperatureQuantity.temperature.unit == Units.CELSIUS

    where:
    unitStringParam | _
    /"℃"/           | _
    /"\u2103"/      | _ // same as the previous row
    /"°C"/          | _
    /"\u00b0C"/     | _ // same as the previous row
    /"Celsius"/     | _
  }

  void "should work for quantity given as an object - raw quantity"() {
    given:
    String stringToDeserialize = """
      {
        "otherName": "someName",
        "length": {
          "value": $quantityValueParam,
          "unit": "$quantityUnitParam"
        }
      }
      """

    when:
    MyBeanRawQuantity deserializedMyBean = objectMapper.readValue(stringToDeserialize, MyBeanRawQuantity)

    then:
    deserializedMyBean.otherName == "someName"
    deserializedMyBean.length == Quantities.getQuantity(1234, Units.METRE)
    deserializedMyBean.length.scale == Quantity.Scale.ABSOLUTE

    where:
    quantityValueParam | quantityUnitParam
    1_234              | "m"
    1_234              | " m"
    1_234              | "m "
    1_234              | " m "
    123_400            | "cm"
  }

  void "should work for quantity given as an object - raw quantity with scale"() {
    given:
    String stringToDeserialize = """
      {
        "otherName": "someName",
        "length": {
          "value": 1234,
          "unit": "m",
          "scale": $quantityScaleStringParam
        }
      }
      """

    when:
    MyBeanRawQuantity deserializedMyBean = objectMapper.readValue(stringToDeserialize, MyBeanRawQuantity)

    then:
    deserializedMyBean.otherName == "someName"
    deserializedMyBean.length == Quantities.getQuantity(1234, Units.METRE)
    deserializedMyBean.length.scale == quantityScaleParam

    where:
    quantityScaleStringParam | quantityScaleParam
    null                     | Quantity.Scale.ABSOLUTE
    /"absolute"/             | Quantity.Scale.ABSOLUTE
    /"relative"/             | Quantity.Scale.RELATIVE
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
    weightParam                   | _
    '""'                          | _
    '"   "'                       | _
    "123"                         | _
    "[123,456]"                   | _
    '{"value":null,"unit":"kg"}'  | _
    '{"value":"","unit":"kg"}'    | _
    '{"value":"  ","unit":"kg"}'  | _
    '{"value":[123],"unit":"kg"}' | _
    '{"value":123,"unit":null}'   | _
    '{"value":123,"unit":""}'     | _
    '{"value":123,"unit":"  "}'   | _
    '{"value":123,"unit":123}'    | _
    '{"value":123,"unit":["kg"]}' | _
  }

  void "should throw when provided dimension does not match expected dimension - typed quantity as string"() {
    given:
    String stringToDeserialize = """
      {
        "name": "someName",
        "weight": "1234 m"
      }
      """

    when:
    objectMapper.readValue(stringToDeserialize, MyBeanTypedQuantity)

    then:
    JsonMappingException jsonMappingException = thrown()
    jsonMappingException.message.startsWith("Failed to parse unexpected dimension of a quantity [quantity: 1234 m, expected dimension: javax.measure.quantity.Mass].")
    jsonMappingException.cause instanceof MeasurementParseException
  }

  void "should throw when provided dimension does not match expected dimension - typed quantity as object"() {
    given:
    String stringToDeserialize = """
      {
        "name": "someName",
        "weight": {
          "value": 1234,
          "unit": "m"
        }
      }
      """

    when:
    objectMapper.readValue(stringToDeserialize, MyBeanTypedQuantity)

    then:
    JsonMappingException jsonMappingException = thrown()
    jsonMappingException.message.startsWith("Failed to parse unexpected dimension of a quantity [quantity: 1234 m, expected dimension: javax.measure.quantity.Mass].")
    jsonMappingException.cause instanceof MeasurementParseException
  }

  void "should throw for non parsable values - typed quantity as string"() {
    given:
    String stringToDeserialize = """
      {
        "name": "someName",
        "weight": "abc kg"
      }
      """

    when:
    objectMapper.readValue(stringToDeserialize, MyBeanTypedQuantity)

    then:
    JsonMappingException jsonMappingException = thrown()
    jsonMappingException.message.startsWith(/Failed to parse Quantity from '"abc kg"'./)
    jsonMappingException.cause instanceof MeasurementParseException
  }

  void "should throw for non parsable values - typed quantity as object"() {
    given:
    String stringToDeserialize = """
      {
        "name": "someName",
        "weight": {
          "value": "abc",
          "unit": "m"
        }
      }
      """

    when:
    objectMapper.readValue(stringToDeserialize, MyBeanTypedQuantity)

    then:
    JsonMappingException jsonMappingException = thrown()
    jsonMappingException.message.startsWith(/Failed to parse Quantity from '{"value":"abc","unit":"m"}'./)
    jsonMappingException.cause instanceof MeasurementParseException
  }

  void "should throw for unknown unit - typed quantity as string"() {
    given:
    String stringToDeserialize = """
      {
        "name": "someName",
        "weight": "123 abc"
      }
      """

    when:
    objectMapper.readValue(stringToDeserialize, MyBeanTypedQuantity)

    then:
    JsonMappingException jsonMappingException = thrown()
    jsonMappingException.message.startsWith(/Failed to parse Quantity from '"123 abc"'./)
    jsonMappingException.cause instanceof MeasurementParseException
  }

  void "should throw for unknown unit - typed quantity as object"() {
    given:
    String stringToDeserialize = """
      {
        "name": "someName",
        "weight": {
          "value": 1234,
          "unit": "abc"
        }
      }
      """

    when:
    objectMapper.readValue(stringToDeserialize, MyBeanTypedQuantity)

    then:
    JsonMappingException jsonMappingException = thrown()
    jsonMappingException.message.startsWith(/Failed to parse Quantity from '{"value":1234,"unit":"abc"}'./)
    jsonMappingException.cause instanceof MeasurementParseException
  }
}
