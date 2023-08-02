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
package org.klokwrk.lib.jackson.springboot

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
import org.klokwrk.lang.groovy.json.RawJsonWrapper
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper
import org.springframework.context.ApplicationContext
import org.springframework.test.context.MergedContextConfiguration
import org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate
import org.springframework.test.context.support.AnnotationConfigContextLoader
import org.springframework.test.context.support.DefaultBootstrapContext
import spock.lang.Specification
import spock.util.environment.RestoreSystemProperties

import javax.measure.Quantity

@JsonTest
class EssentialJacksonCustomizerCustomSetupSpecification extends Specification {

  /**
   * Creates a new Spring Boot application context for this test class.
   * <p/>
   * Creation of this new application context can be triggered from any test method which provides the ability to influence on the Environment. In combination with java system properties, we now
   * have the ability to exercise and test various combinations of configuration properties. This is in contrast with standard spring means where variations in properties used in tests are allowed
   * to be specified only on test class level.
   * <p/>
   * Should be used sparingly for performance reasons. It is much better to use standard Spring (Boot) means if appropriate.
   */
  ApplicationContext makeNewTestApplicationContext() {
    SpringBootTestContextBootstrapper contextBootstrapper = new SpringBootTestContextBootstrapper()
    contextBootstrapper.bootstrapContext = new DefaultBootstrapContext(EssentialJacksonCustomizerCustomSetupSpecification, new DefaultCacheAwareContextLoaderDelegate())
    MergedContextConfiguration contextConfiguration = contextBootstrapper.buildMergedContextConfiguration()
    ApplicationContext applicationContext = new AnnotationConfigContextLoader().loadContext(contextConfiguration)

    return applicationContext
  }

  @RestoreSystemProperties
  void "should be disabled when configured so"() {
    given:
    System.setProperty("klokwrk.jackson.customizer.essential.enabled", "false")
    ApplicationContext applicationContext = makeNewTestApplicationContext()
    ObjectMapper objectMapper = applicationContext.getBean(ObjectMapper)

    expect:
    //noinspection GroovyPointlessBoolean
    objectMapper.isEnabled(JsonParser.Feature.ALLOW_COMMENTS) == false
  }

  @RestoreSystemProperties
  void "should not add StringSanitizingDeserializer when configured so"() {
    given:
    System.setProperty("klokwrk.jackson.customizer.essential.deserialization.stringSanitizingDeserializer.enabled", "false")
    ApplicationContext applicationContext = makeNewTestApplicationContext()
    ObjectMapper objectMapper = applicationContext.getBean(ObjectMapper)

    when:
    Deserializers jsonDeserializers = (objectMapper.deserializationContext.factory as BeanDeserializerFactory).factoryConfig.deserializers().find({ Deserializers deserializers ->
      deserializers.findBeanDeserializer(objectMapper.constructType(String), null, null)
    }) as Deserializers

    then:
    jsonDeserializers == null
  }

  @RestoreSystemProperties
  void "should not add UomQuantityDeserializer when configured so"() {
    System.setProperty("klokwrk.jackson.customizer.essential.deserialization.uomQuantityDeserializer.enabled", "false")
    ApplicationContext applicationContext = makeNewTestApplicationContext()
    ObjectMapper objectMapper = applicationContext.getBean(ObjectMapper)

    when:
    Deserializers jsonDeserializers = (objectMapper.deserializationContext.factory as BeanDeserializerFactory).factoryConfig.deserializers().find({ Deserializers deserializers ->
      deserializers.findBeanDeserializer(objectMapper.constructType(Quantity), null, null)
    }) as Deserializers

    then:
    jsonDeserializers == null
  }

  @RestoreSystemProperties
  void "should not add RawJsonWrapperDeserializer when configured so"() {
    System.setProperty("klokwrk.jackson.customizer.essential.deserialization.rawJsonWrapperDeserializer.enabled", "false")
    ApplicationContext applicationContext = makeNewTestApplicationContext()
    ObjectMapper objectMapper = applicationContext.getBean(ObjectMapper)

    when:
    Deserializers jsonDeserializers = (objectMapper.deserializationContext.factory as BeanDeserializerFactory).factoryConfig.deserializers().find({ Deserializers deserializers ->
      deserializers.findBeanDeserializer(objectMapper.constructType(RawJsonWrapper), null, null)
    }) as Deserializers

    then:
    jsonDeserializers == null
  }

  @RestoreSystemProperties
  void "should not add GStringSerializer when configured so"() {
    given:
    System.setProperty("klokwrk.jackson.customizer.essential.serialization.gStringSerializer.enabled", "false")
    ApplicationContext applicationContext = makeNewTestApplicationContext()
    ObjectMapper objectMapper = applicationContext.getBean(ObjectMapper)

    when:
    Serializers jsonSerializers = (objectMapper.serializerFactory as BeanSerializerFactory).factoryConfig.serializers().find({ Serializers serializers ->
      serializers.findSerializer(null, objectMapper.constructType(GString), null)
    }) as Serializers

    then:
    jsonSerializers == null
  }

  @RestoreSystemProperties
  void "should not add UomQuantitySerializer when configured so"() {
    given:
    System.setProperty("klokwrk.jackson.customizer.essential.serialization.uomQuantitySerializer.enabled", "false")
    ApplicationContext applicationContext = makeNewTestApplicationContext()
    ObjectMapper objectMapper = applicationContext.getBean(ObjectMapper)

    when:
    Serializers jsonSerializers = (objectMapper.serializerFactory as BeanSerializerFactory).factoryConfig.serializers().find({ Serializers serializers ->
      serializers.findSerializer(null, objectMapper.constructType(Quantity), null)
    }) as Serializers

    then:
    jsonSerializers == null
  }

  @RestoreSystemProperties
  void "should not add RawJsonWrapperSerializer when configured so"() {
    given:
    System.setProperty("klokwrk.jackson.customizer.essential.serialization.rawJsonWrapperSerializer.enabled", "false")
    ApplicationContext applicationContext = makeNewTestApplicationContext()
    ObjectMapper objectMapper = applicationContext.getBean(ObjectMapper)

    when:
    Serializers jsonSerializers = (objectMapper.serializerFactory as BeanSerializerFactory).factoryConfig.serializers().find({ Serializers serializers ->
      serializers.findSerializer(null, objectMapper.constructType(RawJsonWrapper), null)
    }) as Serializers

    then:
    jsonSerializers == null
  }

  @RestoreSystemProperties
  void "should not ignore transients when configured so"() {
    given:
    System.setProperty("klokwrk.jackson.customizer.essential.mapper.ignoreTransient", "false")
    ApplicationContext applicationContext = makeNewTestApplicationContext()
    ObjectMapper objectMapper = applicationContext.getBean(ObjectMapper)

    expect:
    //noinspection GroovyPointlessBoolean
    objectMapper.isEnabled(MapperFeature.PROPAGATE_TRANSIENT_MARKER) == false
  }

  @RestoreSystemProperties
  void "should not ignore read-only properties when configured so"() {
    given:
    System.setProperty("klokwrk.jackson.customizer.essential.mapper.ignoreReadOnly", "false")
    ApplicationContext applicationContext = makeNewTestApplicationContext()
    ObjectMapper objectMapper = applicationContext.getBean(ObjectMapper)

    expect:
    //noinspection GroovyPointlessBoolean
    objectMapper.isEnabled(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS) == false
  }

  @RestoreSystemProperties
  void "should not accept case-insensitive enum names when configured so"() {
    given:
    System.setProperty("klokwrk.jackson.customizer.essential.mapper.acceptCaseInsensitiveEnums", "false")
    ApplicationContext applicationContext = makeNewTestApplicationContext()
    ObjectMapper objectMapper = applicationContext.getBean(ObjectMapper)

    expect:
    //noinspection GroovyPointlessBoolean
    objectMapper.isEnabled(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS) == false
  }

  @RestoreSystemProperties
  void "should not allow json comments when configured so"() {
    given:
    System.setProperty("klokwrk.jackson.customizer.essential.deserialization.allowJsonComments", "false")
    ApplicationContext applicationContext = makeNewTestApplicationContext()
    ObjectMapper objectMapper = applicationContext.getBean(ObjectMapper)

    expect:
    //noinspection GroovyPointlessBoolean
    objectMapper.isEnabled(JsonParser.Feature.ALLOW_COMMENTS) == false
  }

  @RestoreSystemProperties
  void "should not accept single value as array when configured so"() {
    given:
    System.setProperty("klokwrk.jackson.customizer.essential.deserialization.acceptSingleValueAsArray", "false")
    ApplicationContext applicationContext = makeNewTestApplicationContext()
    ObjectMapper objectMapper = applicationContext.getBean(ObjectMapper)

    expect:
    //noinspection GroovyPointlessBoolean
    objectMapper.deserializationConfig.isEnabled(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY) == false
  }

  @RestoreSystemProperties
  void "should fail on unknown properties when configured so"() {
    given:
    System.setProperty("klokwrk.jackson.customizer.essential.deserialization.failOnUnknownProperties", "true")
    ApplicationContext applicationContext = makeNewTestApplicationContext()
    ObjectMapper objectMapper = applicationContext.getBean(ObjectMapper)

    expect:
    //noinspection GroovyPointlessBoolean
    objectMapper.deserializationConfig.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES) == true
  }

  @RestoreSystemProperties
  void "should not skip null values on serialization when configured so"() {
    given:
    System.setProperty("klokwrk.jackson.customizer.essential.serialization.skipNullValues", "false")
    ApplicationContext applicationContext = makeNewTestApplicationContext()
    ObjectMapper objectMapper = applicationContext.getBean(ObjectMapper)

    expect:
    objectMapper.serializationConfig.defaultPropertyInclusion.valueInclusion != JsonInclude.Include.NON_NULL
  }

  @RestoreSystemProperties
  void "should not skip null values on deserialization when configured so"() {
    given:
    System.setProperty("klokwrk.jackson.customizer.essential.deserialization.skipNullValues", "false")
    ApplicationContext applicationContext = makeNewTestApplicationContext()
    ObjectMapper objectMapper = applicationContext.getBean(ObjectMapper)

    expect:
    objectMapper.deserializationConfig.defaultSetterInfo != JsonSetter.Value.forValueNulls(Nulls.SKIP)
  }
}
