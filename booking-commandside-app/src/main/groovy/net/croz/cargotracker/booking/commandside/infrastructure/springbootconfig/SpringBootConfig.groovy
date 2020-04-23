package net.croz.cargotracker.booking.commandside.infrastructure.springbootconfig

import groovy.transform.CompileStatic
import net.croz.cargotracker.infrastructure.library.jackson.springboot.EssentialJacksonCustomizer
import net.croz.cargotracker.infrastructure.library.jackson.springboot.EssentialJacksonCustomizerConfigurationProperties
import net.croz.cargotracker.infrastructure.project.axon.logging.LoggingCommandHandlerEnhancerDefinition
import net.croz.cargotracker.infrastructure.project.axon.logging.LoggingEventSourcingHandlerEnhancerDefinition
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

  @Bean
  HandlerEnhancerDefinition loggingEventSourcingHandlerEnhancerDefinition() {
    return new LoggingEventSourcingHandlerEnhancerDefinition()
  }
}
