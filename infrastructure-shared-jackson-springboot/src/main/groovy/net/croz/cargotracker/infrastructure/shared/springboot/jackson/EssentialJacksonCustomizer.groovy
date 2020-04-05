package net.croz.cargotracker.infrastructure.shared.springboot.jackson

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import net.croz.cargotracker.infrastructure.shared.jackson.databind.deser.StringSanitizingDeserializer
import net.croz.cargotracker.infrastructure.shared.jackson.databind.ser.GStringSerializer
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

/**
 * Implementation of Spring Boot's {@link Jackson2ObjectMapperBuilderCustomizer} which configures Spring Boot's provided {@link ObjectMapper} with a set of essential options.
 */
class EssentialJacksonCustomizer implements Jackson2ObjectMapperBuilderCustomizer, BeanPostProcessor {
  static private final String DEFAULT_SPRING_BOOT_OBJECT_MAPPER_BEAN_NAME = "jacksonObjectMapper"

  @Override
  void customize(Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder) {
    jacksonObjectMapperBuilder
        .deserializers(new StringSanitizingDeserializer()) // TODO dmurat: add config
        .serializers(new GStringSerializer())              // TODO dmurat: add config
        .featuresToEnable(
            MapperFeature.PROPAGATE_TRANSIENT_MARKER,      // TODO dmurat: add config
            JsonParser.Feature.ALLOW_COMMENTS,
            DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY
        )
        .featuresToDisable(
            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES        // TODO dmurat: add config
        )
        .serializationInclusion(JsonInclude.Include.NON_NULL)        // TODO dmurat: add config
  }

  // For objectMapper settings that cannot be configured via builder customization
  @Override
  Object postProcessAfterInitialization(Object bean, String beanName) {
    if (bean instanceof ObjectMapper && beanName == DEFAULT_SPRING_BOOT_OBJECT_MAPPER_BEAN_NAME) {
      // TODO dmurat: add config
      bean.setDefaultSetterInfo(JsonSetter.Value.forValueNulls(Nulls.SKIP))
    }

    return bean
  }
}
