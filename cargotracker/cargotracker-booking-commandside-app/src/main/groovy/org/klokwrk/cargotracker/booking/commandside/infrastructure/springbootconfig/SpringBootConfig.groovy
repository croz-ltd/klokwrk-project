/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 CROZ d.o.o, the original author or authors.
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
import org.axonframework.messaging.annotation.HandlerEnhancerDefinition
import org.klokwrk.cargotracker.lib.axon.cqrs.command.CommandHandlerExceptionInterceptor
import org.klokwrk.cargotracker.lib.axon.logging.LoggingCommandHandlerEnhancerDefinition
import org.klokwrk.cargotracker.lib.axon.logging.LoggingEventSourcingHandlerEnhancerDefinition
import org.klokwrk.lib.jackson.springboot.EssentialJacksonCustomizer
import org.klokwrk.lib.jackson.springboot.EssentialJacksonCustomizerConfigurationProperties
import org.klokwrk.lib.validation.springboot.ValidationConfigurationProperties
import org.klokwrk.lib.validation.springboot.ValidationService
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@EnableConfigurationProperties([EssentialJacksonCustomizerConfigurationProperties, ValidationConfigurationProperties])
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

  @Bean
  CommandHandlerExceptionInterceptor commandHandlerExceptionInterceptor() {
    return new CommandHandlerExceptionInterceptor()
  }

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Bean
  ValidationService validationService(ValidationConfigurationProperties validationConfigurationProperties) {
    return new ValidationService(validationConfigurationProperties)
  }
}
