package org.klokwrk.cargotracker.booking.queryside.infrastructure.springbootconfig

import groovy.transform.CompileStatic
import org.axonframework.queryhandling.QueryBus
import org.klokwrk.cargotracker.lib.axon.cqrs.query.QueryHandlerExceptionInterceptor
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
    QueryBus queryBus = applicationContext.getBean(QueryBus)
    QueryHandlerExceptionInterceptor queryHandlerExceptionInterceptor = applicationContext.getBean(QueryHandlerExceptionInterceptor)
    queryBus.registerHandlerInterceptor(queryHandlerExceptionInterceptor)
  }
}
