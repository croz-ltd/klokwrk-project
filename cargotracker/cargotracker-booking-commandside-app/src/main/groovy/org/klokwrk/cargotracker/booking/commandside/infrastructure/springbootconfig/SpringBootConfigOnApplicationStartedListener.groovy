package org.klokwrk.cargotracker.booking.commandside.infrastructure.springbootconfig

import groovy.transform.CompileStatic
import org.axonframework.commandhandling.CommandBus
import org.klokwrk.cargotracker.lib.axon.cqrs.command.CommandHandlerExceptionInterceptor
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.stereotype.Component

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
