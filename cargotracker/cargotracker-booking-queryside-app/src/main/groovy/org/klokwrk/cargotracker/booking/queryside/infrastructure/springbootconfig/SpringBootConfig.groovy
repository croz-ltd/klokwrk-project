package org.klokwrk.cargotracker.booking.queryside.infrastructure.springbootconfig

import groovy.transform.CompileStatic
import org.axonframework.common.transaction.TransactionManager
import org.axonframework.messaging.annotation.HandlerEnhancerDefinition
import org.axonframework.spring.messaging.unitofwork.SpringTransactionManager
import org.klokwrk.cargotracker.lib.axon.logging.LoggingQueryHandlerEnhancerDefinition
import org.klokwrk.lib.datasourceproxy.springboot.DataSourceProxyBeanPostProcessor
import org.klokwrk.lib.datasourceproxy.springboot.DataSourceProxyConfigurationProperties
import org.klokwrk.lib.jackson.springboot.EssentialJacksonCustomizer
import org.klokwrk.lib.jackson.springboot.EssentialJacksonCustomizerConfigurationProperties
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.DefaultTransactionDefinition

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

  /**
   * Creates Axon's read-only SpringTransactionManager.
   * <p/>
   * Axon does not use @Transactional annotations for determining transactional attributes of its message handling methods. By default it uses vanilla {@link DefaultTransactionDefinition}.
   * This is unfortunate since we do not have a way for specifying transaction attributes on a level of message handling method. However, since we are running standalone queryside application,
   * we can set read-only attribute for all transactions created by Axon's transaction manager. This is exactly what this bean does.
   * <p/>
   * Read-only attribute reduces consumption of memory on hibernate level (https://vladmihalcea.com/spring-read-only-transaction-hibernate-optimization/) and reduces some latency and required
   * resources on database level since creation of real transaction IDs is not necessary. The following link explains this better for MySQL under section 'What is a transaction?' -
   * https://www.percona.com/blog/2019/07/15/mysql-the-impact-of-transactions-on-query-throughput/ .
   */
  @Bean
  TransactionManager axonTransactionManager(PlatformTransactionManager transactionManager) {
    TransactionDefinition transactionDefinition = new DefaultTransactionDefinition()
    transactionDefinition.readOnly = true

    return new SpringTransactionManager(transactionManager, transactionDefinition)
  }
}
