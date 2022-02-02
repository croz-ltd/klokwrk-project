package org.klokwrk.cargotracker.booking.queryside.rdbms.projection.model

import groovy.transform.CompileStatic
import org.klokwrk.lib.datasourceproxy.springboot.DataSourceProxyBeanPostProcessor
import org.klokwrk.lib.datasourceproxy.springboot.DataSourceProxyConfigurationProperties
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@EnableConfigurationProperties([DataSourceProxyConfigurationProperties])
@SpringBootApplication
@CompileStatic
class TestApplication {
  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Bean
  BeanPostProcessor dataSourceProxyBeanPostProcessor(DataSourceProxyConfigurationProperties dataSourceProxyConfigurationProperties) {
    return new DataSourceProxyBeanPostProcessor(dataSourceProxyConfigurationProperties)
  }
}
