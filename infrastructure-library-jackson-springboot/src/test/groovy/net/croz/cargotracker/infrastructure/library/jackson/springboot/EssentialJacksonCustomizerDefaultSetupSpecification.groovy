package net.croz.cargotracker.infrastructure.library.jackson.springboot

import com.fasterxml.jackson.annotation.JsonInclude
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
import net.croz.cargotracker.infrastructure.library.jackson.databind.deser.StringSanitizingDeserializer
import net.croz.cargotracker.infrastructure.library.jackson.databind.ser.GStringSerializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import spock.lang.Specification
import spock.lang.Unroll

@JsonTest
class EssentialJacksonCustomizerDefaultSetupSpecification extends Specification {
  @Autowired
  ObjectMapper objectMapper

  @Unroll
  def "objectMapper - should have default deserialization features configured - #deserializationFeature - #isEnabled"() {
    expect:
    objectMapper.getDeserializationConfig().isEnabled(deserializationFeature) == isEnabled

    where:
    deserializationFeature                              | isEnabled
    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES   | false
    DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY | true
  }

  @Unroll
  def "objectMapper - should have default serialization features as defined by Spring Boot configured - #serializationFeature - #isEnabled"() {
    expect:
    objectMapper.getSerializationConfig().isEnabled(serializationFeature) == isEnabled

    where:
    serializationFeature                               | isEnabled
    SerializationFeature.WRITE_DATES_AS_TIMESTAMPS     | false
    SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS | false
  }

  @Unroll
  def "objectMapper - should have default mapper features configured - #mapperFeature - #isEnabled"() {
    expect:
    objectMapper.isEnabled(mapperFeature) == isEnabled

    where:
    mapperFeature                            | isEnabled
    MapperFeature.PROPAGATE_TRANSIENT_MARKER | true
  }

  @Unroll
  def "objectMapper - should have default json parser features configured - #jsonParserFeature - #isEnabled"() {
    expect:
    objectMapper.isEnabled(jsonParserFeature) == isEnabled

    where:
    jsonParserFeature                 | isEnabled
    JsonParser.Feature.ALLOW_COMMENTS | true
  }

  def "objectMapper - should have default serialization inclusion configured to NON_NULL"() {
    expect:
    objectMapper.getSerializationConfig().getDefaultPropertyInclusion().getValueInclusion() == JsonInclude.Include.NON_NULL
  }

  def "objectMapper - should have default StringSanitizingDeserializer configured"() {
    given:
    Deserializers jsonDeserializers = (objectMapper.getDeserializationContext().getFactory() as BeanDeserializerFactory).factoryConfig.deserializers().find({ Deserializers deserializers ->
      deserializers.findBeanDeserializer(objectMapper.constructType(String), null, null)
    }) as Deserializers

    JsonDeserializer jsonDeserializer = jsonDeserializers.findBeanDeserializer(objectMapper.constructType(String), null, null)

    expect:
    jsonDeserializer instanceof StringSanitizingDeserializer
  }

  def "objectMapper - should have default GStringSerializer configured"() {
    given:
    Serializers jsonSerializers = (objectMapper.getSerializerFactory() as BeanSerializerFactory).factoryConfig.serializers().find({ Serializers serializers ->
      serializers.findSerializer(null, objectMapper.constructType(GString), null)
    }) as Serializers

    JsonSerializer jsonSerializer = jsonSerializers.findSerializer(null, objectMapper.constructType(GString), null)

    expect:
    jsonSerializer instanceof GStringSerializer
  }
}
