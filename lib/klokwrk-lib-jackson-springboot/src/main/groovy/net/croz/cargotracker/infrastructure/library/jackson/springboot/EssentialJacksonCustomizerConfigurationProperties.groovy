package net.croz.cargotracker.infrastructure.library.jackson.springboot

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
 *   &#64;SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
 *   &#64;Bean
 *   EssentialJacksonCustomizer essentialJacksonCustomizer(EssentialJacksonCustomizerConfigurationProperties essentialJacksonCustomizerConfigurationProperties) {
 *     return new EssentialJacksonCustomizer(essentialJacksonCustomizerConfigurationProperties)
 *   }
 * }
 * </pre>
 */
@SuppressWarnings("ConfigurationProperties")
@ConfigurationProperties(prefix = "cargotracker.jackson.customizer.essential")
@CompileStatic
class EssentialJacksonCustomizerConfigurationProperties {
  /**
   * By default EssentialJacksonCustomizer is enabled. Set to <code>false</code> to disable it.
   */
  Boolean enabled = true

  Deserialization deserialization = new Deserialization()
  class Deserialization {

    StringSanitizingDeserializer stringSanitizingDeserializer = new StringSanitizingDeserializer()
    class StringSanitizingDeserializer {
      /**
       * By default StringSanitizingDeserializer is enabled. Set to <code>false</code> to disable it.
       */
      Boolean enabled = true
    }

    /**
     * By default json comments are allowed. Set to <code>false</code> to disable this behavior.
     */
    Boolean allowJsonComments = true

    /**
     * By default accepting a single value into an array during deserialization is allowed. Set to <code>false</code> to disable this behavior.
     */
    Boolean acceptSingleValueAsArray = true

    /**
     * By default failing on unknown properties is disabled. Set to <code>true</code> to enable this behavior.
     */
    Boolean failOnUnknownProperties = false

    /**
     * By default skipping null values during deserialization is enabled. Set to <code>false</code> to disable this behavior.
     */
    Boolean skipNullValues = true
  }

  Serialization serialization = new Serialization()
  class Serialization {

    GStringSerializer gStringSerializer = new GStringSerializer()
    class GStringSerializer {
      /**
       * By default GStringSerializer is enabled. Set to <code>false</code> to disable it.
       */
      Boolean enabled = true
    }

    /**
     * By default skipping null values during serialization is enabled. Set to <code>false</code> to disable this behavior.
     */
    Boolean skipNullValues = true
  }

  @SuppressWarnings("unused")
  Mapper mapper = new Mapper()
  class Mapper {
    /**
     * By default transient fields and properties are ignored. Set to <code>false</code> to disable this behavior.
     */
    Boolean ignoreTransient = true
  }
}
