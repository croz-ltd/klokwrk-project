package net.croz.cargotracker.booking.commandside.infrastructure.springbootconfig

import groovy.transform.CompileStatic
import net.croz.cargotracker.infrastructure.shared.axon.logging.LoggingCommandHandlerEnhancerDefinition
import net.croz.cargotracker.infrastructure.shared.springboot.jackson.EssentialJacksonCustomizer
import net.croz.cargotracker.infrastructure.shared.springboot.jackson.EssentialJacksonCustomizerConfigurationProperties
import org.axonframework.messaging.annotation.HandlerEnhancerDefinition
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@EnableConfigurationProperties(EssentialJacksonCustomizerConfigurationProperties)
@Configuration
@CompileStatic
class SpringBootConfig {

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Bean
  EssentialJacksonCustomizer essentialJacksonCustomizer(EssentialJacksonCustomizerConfigurationProperties essentialJacksonCustomizerConfigurationProperties) {
    return new EssentialJacksonCustomizer(essentialJacksonCustomizerConfigurationProperties)
  }

  @Bean
  HandlerEnhancerDefinition loggingCommandHandlerEnhancerDefinition() {
    return new LoggingCommandHandlerEnhancerDefinition()
  }
}
