/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracker.booking.commandside.infrastructure.springbootconfig

import groovy.transform.CompileStatic
import org.axonframework.commandhandling.CommandBus
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.commandhandling.gateway.DefaultCommandGateway
import org.axonframework.commandhandling.gateway.IntervalRetryScheduler
import org.axonframework.commandhandling.gateway.RetryScheduler
import org.axonframework.messaging.annotation.HandlerEnhancerDefinition
import org.klokwrk.cargotracker.booking.domain.model.service.CargoCreatorService
import org.klokwrk.cargotracker.booking.domain.model.service.ConstantBasedMaxAllowedTeuCountPolicy
import org.klokwrk.cargotracker.booking.domain.model.service.DefaultCargoCreatorService
import org.klokwrk.cargotracker.booking.domain.model.service.MaxAllowedTeuCountPolicy
import org.klokwrk.cargotracker.booking.domain.model.service.MaxAllowedWeightPerContainerPolicy
import org.klokwrk.cargotracker.booking.domain.model.service.PercentBasedMaxAllowedWeightPerContainerPolicy
import org.klokwrk.cargotracker.booking.out.customer.adapter.InMemoryCustomerRegistryService
import org.klokwrk.cargotracker.lib.axon.cqrs.command.CommandHandlerExceptionInterceptor
import org.klokwrk.cargotracker.lib.axon.cqrs.command.NonTransientFailurePredicate
import org.klokwrk.cargotracker.lib.axon.logging.LoggingCommandHandlerEnhancerDefinition
import org.klokwrk.cargotracker.lib.axon.logging.LoggingEventSourcingHandlerEnhancerDefinition
import org.klokwrk.lib.jackson.springboot.EssentialJacksonCustomizer
import org.klokwrk.lib.jackson.springboot.EssentialJacksonCustomizerConfigurationProperties
import org.klokwrk.lib.validation.springboot.ValidationConfigurationProperties
import org.klokwrk.lib.validation.springboot.ValidationService
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

@EnableConfigurationProperties([EssentialJacksonCustomizerConfigurationProperties, ValidationConfigurationProperties])
@Configuration(proxyBeanMethods = false)
@CompileStatic
class SpringBootConfig {

  static final Integer MAX_RETRY_COUNT_DEFAULT = 3
  static final Integer RETRY_INTERVAL_MILLIS_DEFAULT = 1000
  private static final Integer RETRY_EXECUTOR_POOL_SIZE_DEFAULT = 4

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

  @Bean
  CommandHandlerExceptionInterceptor commandHandlerExceptionInterceptor() {
    return new CommandHandlerExceptionInterceptor()
  }

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Bean
  ValidationService validationService(ValidationConfigurationProperties validationConfigurationProperties) {
    return new ValidationService(validationConfigurationProperties)
  }

  @Bean
  MaxAllowedWeightPerContainerPolicy maxAllowedWeightPerContainerPolicy() {
    return new PercentBasedMaxAllowedWeightPerContainerPolicy(95)
  }

  @Bean
  CargoCreatorService cargoCreatorService(MaxAllowedWeightPerContainerPolicy maxAllowedWeightPerContainerPolicy) {
    return new DefaultCargoCreatorService(maxAllowedWeightPerContainerPolicy)
  }

  @Bean
  MaxAllowedTeuCountPolicy maxAllowedTeuCountPolicy() {
    return new ConstantBasedMaxAllowedTeuCountPolicy(5_000.0)
  }

  @Bean
  InMemoryCustomerRegistryService inMemoryCustomerRegistryService() {
    return new InMemoryCustomerRegistryService()
  }

  @Bean
  CommandGateway defaultCommandGateway(CommandBus commandBus) {
    ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(RETRY_EXECUTOR_POOL_SIZE_DEFAULT)
    RetryScheduler retryScheduler = IntervalRetryScheduler
        .builder()
        .retryExecutor(scheduledExecutorService)
        .nonTransientFailurePredicate(new NonTransientFailurePredicate())
        .maxRetryCount(MAX_RETRY_COUNT_DEFAULT)
        .retryInterval(RETRY_INTERVAL_MILLIS_DEFAULT)
        .build()

    CommandGateway defaultCommandGateway = DefaultCommandGateway
        .builder()
        .commandBus(commandBus)
        .retryScheduler(retryScheduler)
        .build()

    return defaultCommandGateway
  }
}
