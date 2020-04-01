package net.croz.cargotracker.booking.queryside.infrastructure.springbootconfig

import net.croz.cargotracker.infrastructure.shared.springboot.datasourceproxy.DataSourceProxyBeanPostProcessor
import net.croz.cargotracker.infrastructure.shared.springboot.datasourceproxy.DataSourceProxyConfigurationProperties
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@EnableConfigurationProperties(DataSourceProxyConfigurationProperties)
@Configuration
class SpringBootConfig {

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Bean
  BeanPostProcessor dataSourceProxyBeanPostProcessor(DataSourceProxyConfigurationProperties dataSourceProxyConfigurationProperties) {
    return new DataSourceProxyBeanPostProcessor(dataSourceProxyConfigurationProperties)
  }
}
