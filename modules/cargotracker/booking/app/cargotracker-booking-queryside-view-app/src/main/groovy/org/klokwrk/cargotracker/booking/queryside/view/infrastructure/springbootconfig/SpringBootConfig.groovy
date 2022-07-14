/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracker.booking.queryside.view.infrastructure.springbootconfig

import groovy.transform.CompileStatic
import org.axonframework.common.transaction.TransactionManager
import org.axonframework.messaging.annotation.HandlerEnhancerDefinition
import org.axonframework.spring.messaging.unitofwork.SpringTransactionManager
import org.klokwrk.cargotracker.booking.out.customer.adapter.InMemoryCustomerRegistryService
import org.klokwrk.cargotracker.lib.axon.cqrs.query.QueryHandlerExceptionInterceptor
import org.klokwrk.cargotracker.lib.axon.logging.LoggingQueryHandlerEnhancerDefinition
import org.klokwrk.lib.datasourceproxy.springboot.DataSourceProxyBeanPostProcessor
import org.klokwrk.lib.datasourceproxy.springboot.DataSourceProxyConfigurationProperties
import org.klokwrk.lib.jackson.springboot.EssentialJacksonCustomizer
import org.klokwrk.lib.jackson.springboot.EssentialJacksonCustomizerConfigurationProperties
import org.klokwrk.lib.validation.springboot.ValidationConfigurationProperties
import org.klokwrk.lib.validation.springboot.ValidationService
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.DefaultTransactionDefinition

@EnableConfigurationProperties([DataSourceProxyConfigurationProperties, EssentialJacksonCustomizerConfigurationProperties, ValidationConfigurationProperties])
@Configuration(proxyBeanMethods = false)
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

  @Bean
  QueryHandlerExceptionInterceptor queryHandlerExceptionInterceptor() {
    return new QueryHandlerExceptionInterceptor()
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

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Bean
  ValidationService validationService(ValidationConfigurationProperties validationConfigurationProperties) {
    return new ValidationService(validationConfigurationProperties)
  }

  @Bean
  InMemoryCustomerRegistryService inMemoryCustomerRegistryService() {
    return new InMemoryCustomerRegistryService()
  }
}
