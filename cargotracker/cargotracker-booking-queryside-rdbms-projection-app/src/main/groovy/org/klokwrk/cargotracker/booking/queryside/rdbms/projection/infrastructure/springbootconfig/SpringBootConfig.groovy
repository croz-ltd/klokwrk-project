package org.klokwrk.cargotracker.booking.queryside.rdbms.projection.infrastructure.springbootconfig

import groovy.transform.CompileStatic
import org.axonframework.messaging.annotation.HandlerEnhancerDefinition
import org.klokwrk.cargotracker.lib.axon.logging.LoggingEventHandlerEnhancerDefinition
import org.klokwrk.lib.datasourceproxy.springboot.DataSourceProxyBeanPostProcessor
import org.klokwrk.lib.datasourceproxy.springboot.DataSourceProxyConfigurationProperties
import org.klokwrk.lib.jackson.springboot.EssentialJacksonCustomizer
import org.klokwrk.lib.jackson.springboot.EssentialJacksonCustomizerConfigurationProperties
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
  HandlerEnhancerDefinition loggingEventHandlerEnhancerDefinition() {
    return new LoggingEventHandlerEnhancerDefinition()
  }
}
