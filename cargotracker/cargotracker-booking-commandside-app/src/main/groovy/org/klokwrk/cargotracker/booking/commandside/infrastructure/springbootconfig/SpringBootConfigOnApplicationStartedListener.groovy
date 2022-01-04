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
package org.klokwrk.cargotracker.booking.commandside.infrastructure.springbootconfig

import groovy.transform.CompileStatic
import org.axonframework.commandhandling.CommandBus
import org.klokwrk.cargotracker.lib.axon.cqrs.command.CommandHandlerExceptionInterceptor
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.stereotype.Component

/**
 * On ApplicationStartedEvent listener handling configuration logic that causes racing conditions when executed from {@code Configuration} annotated classes.
 */
@Component
@CompileStatic
class SpringBootConfigOnApplicationStartedListener implements ApplicationListener<ApplicationStartedEvent> {
  @Override
  void onApplicationEvent(ApplicationStartedEvent event) {
    ConfigurableApplicationContext applicationContext = event.applicationContext
    registerHandlerInterceptors(applicationContext)
  }

  protected void registerHandlerInterceptors(ConfigurableApplicationContext applicationContext) {
    CommandBus commandBus = applicationContext.getBean(CommandBus)
    CommandHandlerExceptionInterceptor commandHandlerExceptionInterceptor = applicationContext.getBean(CommandHandlerExceptionInterceptor)
    commandBus.registerHandlerInterceptor(commandHandlerExceptionInterceptor)
  }
}
