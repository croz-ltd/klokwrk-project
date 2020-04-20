package net.croz.cargotracker.booking.queryside.infrastructure.springbootconfig

import groovy.transform.CompileStatic
import net.croz.cargotracker.infrastructure.shared.axon.logging.LoggingQueryHandlerEnhancerDefinition
import net.croz.cargotracker.infrastructure.shared.springboot.datasourceproxy.DataSourceProxyBeanPostProcessor
import net.croz.cargotracker.infrastructure.shared.springboot.datasourceproxy.DataSourceProxyConfigurationProperties
import net.croz.cargotracker.infrastructure.shared.springboot.jackson.EssentialJacksonCustomizer
import net.croz.cargotracker.infrastructure.shared.springboot.jackson.EssentialJacksonCustomizerConfigurationProperties
import org.axonframework.messaging.annotation.HandlerEnhancerDefinition
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@EnableConfigurationProperties([DataSourceProxyConfigurationProperties, EssentialJacksonCustomizerConfigurationProperties])
@Configuration
@CompileStatic
class SpringBootConfig {
  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Bean
  BeanPostProcessor dataSourceProxyBeanPostProcessor(DataSourceProxyConfigurationProperties dataSourceProxyConfigurationProperties) {
    return new DataSourceProxyBeanPostProcessor(dataSourceProxyConfigurationProperties)
  }

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Bean
  EssentialJacksonCustomizer essentialJacksonCustomizer(EssentialJacksonCustomizerConfigurationProperties essentialJacksonCustomizerConfigurationProperties) {
    return new EssentialJacksonCustomizer(essentialJacksonCustomizerConfigurationProperties)
  }

  @Bean
  HandlerEnhancerDefinition loggingQueryHandlerEnhancerDefinition() {
    return new LoggingQueryHandlerEnhancerDefinition()
  }
}