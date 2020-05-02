package net.croz.cargotracker.infrastructure.library.jackson.springboot

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.BeanDeserializerFactory
import com.fasterxml.jackson.databind.deser.Deserializers
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory
import com.fasterxml.jackson.databind.ser.Serializers
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper
import org.springframework.context.ApplicationContext
import org.springframework.test.context.MergedContextConfiguration
import org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate
import org.springframework.test.context.support.AnnotationConfigContextLoader
import org.springframework.test.context.support.DefaultBootstrapContext
import spock.lang.Specification
import spock.util.environment.RestoreSystemProperties

@JsonTest
class EssentialJacksonCustomizerCustomSetupSpecification extends Specification {

  /**
   * Creates a new Spring Boot application context for this test class.
   * </p>
   * Creation of this new application context can be triggered from any test method which provides the ability to influence on the Environment. In combination with java system properties, we now
   * have the ability to exercise and test various combinations of configuration properties. This is in contrast with standard spring means where variations in properties used in tests are allowed
   * to be specified only on test class level.
   * <p/>
   * Should be used sparingly for performance reasons. It is much better to use standard Spring (Boot) means if appropriate.
   */
  ApplicationContext createNewTestApplicationContext() {
    SpringBootTestContextBootstrapper contextBootstrapper = new SpringBootTestContextBootstrapper()
    contextBootstrapper.setBootstrapContext(new DefaultBootstrapContext(EssentialJacksonCustomizerCustomSetupSpecification, new DefaultCacheAwareContextLoaderDelegate()))
    MergedContextConfiguration contextConfiguration = contextBootstrapper.buildMergedContextConfiguration()
    ApplicationContext applicationContext = new AnnotationConfigContextLoader().loadContext(contextConfiguration)

    return applicationContext
  }

  @RestoreSystemProperties
  void "should be disabled when configured so"() {
    given:
    System.setProperty("cargotracker.jackson.customizer.essential.enabled", "false")
    ApplicationContext applicationContext = createNewTestApplicationContext()
    ObjectMapper objectMapper = applicationContext.getBean(ObjectMapper)

    expect:
    //noinspection GroovyPointlessBoolean
    objectMapper.isEnabled(JsonParser.Feature.ALLOW_COMMENTS) == false
  }

  @RestoreSystemProperties
  void "should not add StringSanitizingDeserializer when configured so"() {
    given:
    System.setProperty("cargotracker.jackson.customizer.essential.deserialization.stringSanitizingDeserializer.enabled", "false")
    ApplicationContext applicationContext = createNewTestApplicationContext()
    ObjectMapper objectMapper = applicationContext.getBean(ObjectMapper)

    when:
    Deserializers jsonDeserializers = (objectMapper.deserializationContext.factory as BeanDeserializerFactory).factoryConfig.deserializers().find({ Deserializers deserializers ->
      deserializers.findBeanDeserializer(objectMapper.constructType(String), null, null)
    }) as Deserializers

    then:
    jsonDeserializers == null
  }

  @RestoreSystemProperties
  void "should not add GStringSerializer when configured so"() {
    given:
    System.setProperty("cargotracker.jackson.customizer.essential.serialization.gStringSerializer.enabled", "false")
    ApplicationContext applicationContext = createNewTestApplicationContext()
    ObjectMapper objectMapper = applicationContext.getBean(ObjectMapper)

    when:
    Serializers jsonSerializers = (objectMapper.serializerFactory as BeanSerializerFactory).factoryConfig.serializers().find({ Serializers serializers ->
      serializers.findSerializer(null, objectMapper.constructType(GString), null)
    }) as Serializers

    then:
    jsonSerializers == null
  }

  @RestoreSystemProperties
  void "should not ignore transients when configured so"() {
    given:
    System.setProperty("cargotracker.jackson.customizer.essential.mapper.ignoreTransient", "false")
    ApplicationContext applicationContext = createNewTestApplicationContext()
    ObjectMapper objectMapper = applicationContext.getBean(ObjectMapper)

    expect:
    //noinspection GroovyPointlessBoolean
    objectMapper.isEnabled(MapperFeature.PROPAGATE_TRANSIENT_MARKER) == false
  }

  @RestoreSystemProperties
  void "should not allow json comments when configured so"() {
    given:
    System.setProperty("cargotracker.jackson.customizer.essential.deserialization.allowJsonComments", "false")
    ApplicationContext applicationContext = createNewTestApplicationContext()
    ObjectMapper objectMapper = applicationContext.getBean(ObjectMapper)

    expect:
    //noinspection GroovyPointlessBoolean
    objectMapper.isEnabled(JsonParser.Feature.ALLOW_COMMENTS) == false
  }

  @RestoreSystemProperties
  void "should not accept single value as array when configured so"() {
    given:
    System.setProperty("cargotracker.jackson.customizer.essential.deserialization.acceptSingleValueAsArray", "false")
    ApplicationContext applicationContext = createNewTestApplicationContext()
    ObjectMapper objectMapper = applicationContext.getBean(ObjectMapper)

    expect:
    //noinspection GroovyPointlessBoolean
    objectMapper.deserializationConfig.isEnabled(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY) == false
  }

  @RestoreSystemProperties
  void "should fail on unknown properties when configured so"() {
    given:
    System.setProperty("cargotracker.jackson.customizer.essential.deserialization.failOnUnknownProperties", "true")
    ApplicationContext applicationContext = createNewTestApplicationContext()
    ObjectMapper objectMapper = applicationContext.getBean(ObjectMapper)

    expect:
    //noinspection GroovyPointlessBoolean
    objectMapper.deserializationConfig.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES) == true
  }

  @RestoreSystemProperties
  void "should not skip null values on serialization when configured so"() {
    given:
    System.setProperty("cargotracker.jackson.customizer.essential.serialization.skipNullValues", "false")
    ApplicationContext applicationContext = createNewTestApplicationContext()
    ObjectMapper objectMapper = applicationContext.getBean(ObjectMapper)

    expect:
    objectMapper.serializationConfig.defaultPropertyInclusion.valueInclusion != JsonInclude.Include.NON_NULL
  }

  @RestoreSystemProperties
  void "should not skip null values on deserialization when configured so"() {
    given:
    System.setProperty("cargotracker.jackson.customizer.essential.deserialization.skipNullValues", "false")
    ApplicationContext applicationContext = createNewTestApplicationContext()
    ObjectMapper objectMapper = applicationContext.getBean(ObjectMapper)

    expect:
    objectMapper.deserializationConfig.defaultSetterInfo != JsonSetter.Value.forValueNulls(Nulls.SKIP)
  }
}
