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
package org.klokwrk.lib.hi.jackson.springboot

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
import org.klokwrk.lib.xlang.groovy.base.json.RawJsonWrapper
import org.klokwrk.lib.jackson.databind.deser.RawJsonWrapperDeserializer
import org.klokwrk.lib.jackson.databind.deser.StringSanitizingDeserializer
import org.klokwrk.lib.jackson.databind.deser.UomQuantityDeserializer
import org.klokwrk.lib.jackson.databind.ser.GStringSerializer
import org.klokwrk.lib.jackson.databind.ser.RawJsonWrapperSerializer
import org.klokwrk.lib.jackson.databind.ser.UomQuantitySerializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import spock.lang.Specification

import javax.measure.Quantity

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
    mapperFeature                               | isEnabled
    MapperFeature.PROPAGATE_TRANSIENT_MARKER    | true
    MapperFeature.REQUIRE_SETTERS_FOR_GETTERS   | true
    MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS | true
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

  void "objectMapper - should have default deserializers configured"() {
    given:
    Deserializers jsonDeserializers = (objectMapper.deserializationContext.factory as BeanDeserializerFactory).factoryConfig.deserializers().find({ Deserializers deserializers ->
      deserializers.findBeanDeserializer(objectMapper.constructType(String), null, null)
    }) as Deserializers

    JsonDeserializer stringSanitizingDeserializer = jsonDeserializers.findBeanDeserializer(objectMapper.constructType(String), null, null)
    JsonDeserializer uomQuantityDeserializer = jsonDeserializers.findBeanDeserializer(objectMapper.constructType(Quantity), null, null)
    JsonDeserializer rawJsonWrapperDeserializer = jsonDeserializers.findBeanDeserializer(objectMapper.constructType(RawJsonWrapper), null, null)

    expect:
    stringSanitizingDeserializer instanceof StringSanitizingDeserializer
    uomQuantityDeserializer instanceof UomQuantityDeserializer
    rawJsonWrapperDeserializer instanceof RawJsonWrapperDeserializer
  }

  void "objectMapper - should have default serializers configured"() {
    given:
    Serializers jsonSerializers = (objectMapper.serializerFactory as BeanSerializerFactory).factoryConfig.serializers().find({ Serializers serializers ->
      serializers.findSerializer(null, objectMapper.constructType(GString), null)
    }) as Serializers

    JsonSerializer gStringSerializer = jsonSerializers.findSerializer(null, objectMapper.constructType(GString), null)
    JsonSerializer uomQuantitySerializer = jsonSerializers.findSerializer(null, objectMapper.constructType(Quantity), null)
    JsonSerializer rawJsonWrapperSerializer = jsonSerializers.findSerializer(null, objectMapper.constructType(RawJsonWrapper), null)

    expect:
    gStringSerializer instanceof GStringSerializer
    uomQuantitySerializer instanceof UomQuantitySerializer
    rawJsonWrapperSerializer instanceof RawJsonWrapperSerializer
  }
}
