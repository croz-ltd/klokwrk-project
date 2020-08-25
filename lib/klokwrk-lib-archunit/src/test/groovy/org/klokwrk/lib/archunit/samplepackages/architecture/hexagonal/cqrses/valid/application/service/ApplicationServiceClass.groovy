package org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.valid.application.service

import groovy.transform.CompileStatic
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.valid.application.port.in.ApplicationPortInInterface
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.valid.application.port.out.ApplicationPortOutInterface
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.valid.domain.aggregate.DomainAggregateClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.valid.domain.command.DomainCommandClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.valid.domain.model.DomainModelClass

@SuppressWarnings('unused')
@CompileStatic
class ApplicationServiceClass implements ApplicationPortInInterface {
  DomainModelClass domainModelClass = new DomainModelClass()
  DomainCommandClass domainCommandClass = new DomainCommandClass()
  DomainAggregateClass domainAggregateClass = new DomainAggregateClass()

  ApplicationPortOutInterface applicationPortOutInterface

  ApplicationServiceClass(ApplicationPortOutInterface applicationPortOutInterface) {
    this.applicationPortOutInterface = applicationPortOutInterface
  }
}
