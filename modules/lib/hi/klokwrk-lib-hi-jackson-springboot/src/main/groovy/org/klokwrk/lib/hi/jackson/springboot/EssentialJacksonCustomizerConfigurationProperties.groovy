/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2024 CROZ d.o.o, the original author or authors.
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

import groovy.transform.CompileStatic
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Spring Boot configuration properties for {@link EssentialJacksonCustomizer}.
 * <p/>
 * To be able to use this from Spring Boot application, minimal configuration is required that enables this configuration properties and configures accompanying bean post processor like in following
 * example:
 * <pre>
 * &#64;EnableConfigurationProperties(EssentialJacksonCustomizerConfigurationProperties)
 * &#64;Configuration
 * class SpringBootConfig {
 *   &#64;Bean
 *   static EssentialJacksonCustomizer essentialJacksonCustomizer(ObjectProvider&lt;EssentialJacksonCustomizerConfigurationProperties&gt; essentialJacksonCustomizerConfigurationPropertiesObjectProvider) {
 *     return new EssentialJacksonCustomizer(essentialJacksonCustomizerConfigurationPropertiesObjectProvider)
 *   }
 * }
 * </pre>
 */
@SuppressWarnings("ConfigurationProperties")
@ConfigurationProperties(prefix = "klokwrk.jackson.customizer.essential")
@CompileStatic
class EssentialJacksonCustomizerConfigurationProperties {
  /**
   * By default EssentialJacksonCustomizer is enabled. Set to {@code false} to disable it.
   */
  Boolean enabled = true

  Deserialization deserialization = new Deserialization()
  class Deserialization {

    StringSanitizingDeserializer stringSanitizingDeserializer = new StringSanitizingDeserializer()
    class StringSanitizingDeserializer {
      /**
       * By default StringSanitizingDeserializer is enabled. Set to {@code false} to disable it.
       */
      Boolean enabled = true
    }

    UomQuantityDeserializer uomQuantityDeserializer = new UomQuantityDeserializer()
    class UomQuantityDeserializer {
      /**
       * By default UomQuantityDeserializer is enabled. Set to {@code false} to disable it.
       */
      Boolean enabled = true
    }

    RawJsonWrapperDeserializer rawJsonWrapperDeserializer = new RawJsonWrapperDeserializer()
    class RawJsonWrapperDeserializer {
      /**
       * By default RawJsonWrapperDeserializer is enabled. Set to {@code false} to disable it.
       */
      Boolean enabled = true
    }

    /**
     * By default json comments are allowed. Set to {@code false} to disable this behavior.
     */
    Boolean allowJsonComments = true

    /**
     * By default accepting a single value into an array during deserialization is allowed. Set to {@code false} to disable this behavior.
     */
    Boolean acceptSingleValueAsArray = true

    /**
     * By default failing on unknown properties is disabled. Set to {@code true} to enable this behavior.
     */
    Boolean failOnUnknownProperties = false

    /**
     * By default skipping null values during deserialization is enabled. Set to {@code false} to disable this behavior.
     */
    Boolean skipNullValues = true
  }

  Serialization serialization = new Serialization()
  class Serialization {

    GStringSerializer gStringSerializer = new GStringSerializer()
    class GStringSerializer {
      /**
       * By default GStringSerializer is enabled. Set to {@code false} to disable it.
       */
      Boolean enabled = true
    }

    UomQuantitySerializer uomQuantitySerializer = new UomQuantitySerializer()
    class UomQuantitySerializer {
      /**
       * By default UomQuantitySerializer is enabled. Set to {@code false} to disable it.
       */
      Boolean enabled = true
    }

    RawJsonWrapperSerializer rawJsonWrapperSerializer = new RawJsonWrapperSerializer()
    class RawJsonWrapperSerializer {
      /**
       * By default RawJsonWrapperSerializer is enabled. Set to {@code false} to disable it.
       */
      Boolean enabled = true
    }

    /**
     * By default skipping null values during serialization is enabled. Set to {@code false} to disable this behavior.
     */
    Boolean skipNullValues = true
  }

  Mapper mapper = new Mapper()
  class Mapper {
    /**
     * By default transient fields and properties are ignored. Set to {@code false} to disable this behavior.
     */
    Boolean ignoreTransient = true

    /**
     * By default read-only properties (a.k.a. getter-only or derived properties) are ignored. Set to {@code false} to disable this behavior.
     */
    Boolean ignoreReadOnly = true

    /**
     * By default enum names are treated as case insensitive. Set to {@code false} to disable this behavior.
     */
    Boolean acceptCaseInsensitiveEnums = true
  }
}
