package org.klokwrk.lib.jackson.springboot

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.deser.BeanDeserializerFactory
import com.fasterxml.jackson.databind.deser.Deserializers
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory
import com.fasterxml.jackson.databind.ser.Serializers
import org.klokwrk.lib.jackson.databind.deser.StringSanitizingDeserializer
import org.klokwrk.lib.jackson.databind.ser.GStringSerializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import spock.lang.Specification

@JsonTest
class EssentialJacksonCustomizerDefaultSetupSpecification extends Specification {
  @Autowired
  ObjectMapper objectMapper

  void "objectMapper - should have default deserialization features configured - #deserializationFeature - #isEnabled"() {
    expect:
    objectMapper.deserializationConfig.isEnabled(deserializationFeature) == isEnabled

    where:
    deserializationFeature                              | isEnabled
    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES   | false
    DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY | true
  }

  void "objectMapper - should have default serialization features as defined by Spring Boot configured - #serializationFeature - #isEnabled"() {
    expect:
    objectMapper.serializationConfig.isEnabled(serializationFeature) == isEnabled

    where:
    serializationFeature                               | isEnabled
    SerializationFeature.WRITE_DATES_AS_TIMESTAMPS     | false
    SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS | false
  }

  void "objectMapper - should have default mapper features configured - #mapperFeature - #isEnabled"() {
    expect:
    objectMapper.isEnabled(mapperFeature) == isEnabled

    where:
    mapperFeature                            | isEnabled
    MapperFeature.PROPAGATE_TRANSIENT_MARKER | true
  }

  void "objectMapper - should have default json parser features configured - #jsonParserFeature - #isEnabled"() {
    expect:
    objectMapper.isEnabled(jsonParserFeature) == isEnabled

    where:
    jsonParserFeature                 | isEnabled
    JsonParser.Feature.ALLOW_COMMENTS | true
  }

  void "objectMapper - should have default serialization inclusion configured to NON_NULL"() {
    expect:
    objectMapper.serializationConfig.defaultPropertyInclusion.valueInclusion == JsonInclude.Include.NON_NULL
  }

  void "objectMapper - should have default deserialization setter info configured to Nulls.SKIP"() {
    expect:
    objectMapper.deserializationConfig.defaultSetterInfo == JsonSetter.Value.forValueNulls(Nulls.SKIP)
  }

  void "objectMapper - should have default StringSanitizingDeserializer configured"() {
    given:
    Deserializers jsonDeserializers = (objectMapper.deserializationContext.factory as BeanDeserializerFactory).factoryConfig.deserializers().find({ Deserializers deserializers ->
      deserializers.findBeanDeserializer(objectMapper.constructType(String), null, null)
    }) as Deserializers

    JsonDeserializer jsonDeserializer = jsonDeserializers.findBeanDeserializer(objectMapper.constructType(String), null, null)

    expect:
    jsonDeserializer instanceof StringSanitizingDeserializer
  }

  void "objectMapper - should have default GStringSerializer configured"() {
    given:
    Serializers jsonSerializers = (objectMapper.serializerFactory as BeanSerializerFactory).factoryConfig.serializers().find({ Serializers serializers ->
      serializers.findSerializer(null, objectMapper.constructType(GString), null)
    }) as Serializers

    JsonSerializer jsonSerializer = jsonSerializers.findSerializer(null, objectMapper.constructType(GString), null)

    expect:
    jsonSerializer instanceof GStringSerializer
  }
}
