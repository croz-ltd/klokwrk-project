package org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.valid.domain.aggregate

import groovy.transform.CompileStatic
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.valid.application.port.out.ApplicationPortOutInterface
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.valid.domain.command.DomainCommandClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.valid.domain.event.DomainEventClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.valid.domain.model.DomainModelClass

@SuppressWarnings('unused')
@CompileStatic
class DomainAggregateClass {
  DomainModelClass domainModelClass = new DomainModelClass()
  DomainEventClass domainEventClass = new DomainEventClass()
  DomainCommandClass domainCommandClass = new DomainCommandClass()

  ApplicationPortOutInterface applicationPortOutInterface

  DomainAggregateClass() {
  }

  DomainAggregateClass(ApplicationPortOutInterface applicationPortOutInterface) {
    this.applicationPortOutInterface = applicationPortOutInterface
  }
}
