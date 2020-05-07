package org.klokwrk.lib.jackson.databind.deser

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import spock.lang.Specification
import spock.lang.Unroll

class StringSanitizingDeserializerSpecification extends Specification {
  ObjectMapper objectMapper

  void setup() {
    SimpleModule simpleModule = new SimpleModule()
    simpleModule.addDeserializer(String, new StringSanitizingDeserializer())

    ObjectMapper objectMapper = new ObjectMapper()
    objectMapper.registerModule(simpleModule)

    this.objectMapper = objectMapper
  }

  @Unroll
  void "should deserialize empty string into null - string value is #aggregateIdentifierStringValue"() {
    given:
    String stringToDeserialize = """
      {
        "aggregateIdentifier": ${ aggregateIdentifierStringValue },
        "originLocation": "myOrigin",
        "destinationLocation": "myDestination"
      }
      """

    when:
    Map<String, ?> deserializedMap = objectMapper.readValue(stringToDeserialize, Map)

    then:
    deserializedMap.aggregateIdentifier == null

    where:
    aggregateIdentifierStringValue | _
    '""'                           | _
    '"    "'                       | _
  }

  void "should deserialize null into null"() {
    given:
    String stringToDeserialize = """
      {
        "aggregateIdentifier": null,
        "originLocation": "myOrigin",
        "destinationLocation": "myDestination"
      }
      """

    when:
    Map<String, ?> deserializedMap = objectMapper.readValue(stringToDeserialize, Map)

    then:
    deserializedMap.aggregateIdentifier == null
  }
}
