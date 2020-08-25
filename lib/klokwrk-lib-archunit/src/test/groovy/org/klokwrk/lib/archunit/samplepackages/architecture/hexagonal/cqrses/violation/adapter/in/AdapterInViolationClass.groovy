package org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.adapter.in

import groovy.transform.CompileStatic
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.adapter.out.AdapterOutViolationClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.adapter.projection.AdapterProjectionViolationClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.application.port.out.ApplicationPortOutViolationInterface
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.application.service.ApplicationServiceViolationClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.domain.aggregate.DomainAggregateViolationClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.domain.command.DomainCommandViolationClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.domain.event.DomainEventViolationClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.domain.model.DomainModelViolationClass

@SuppressWarnings('unused')
@CompileStatic
class AdapterInViolationClass {
  DomainModelViolationClass domainModelViolationClass = new DomainModelViolationClass()
  DomainEventViolationClass domainEventViolationClass = new DomainEventViolationClass()
  DomainCommandViolationClass domainCommandViolationClass = new DomainCommandViolationClass()
  DomainAggregateViolationClass domainAggregateViolationClass = new DomainAggregateViolationClass()

  ApplicationPortOutViolationInterface applicationPortOutViolationInterface
  ApplicationServiceViolationClass applicationServiceViolationClass = new ApplicationServiceViolationClass()

  AdapterOutViolationClass adapterOutViolationClass = new AdapterOutViolationClass()
  AdapterProjectionViolationClass adapterProjectionViolationClass = new AdapterProjectionViolationClass()

  AdapterInViolationClass() {
  }

  AdapterInViolationClass(ApplicationPortOutViolationInterface applicationPortOutViolationInterface) {
    this.applicationPortOutViolationInterface = applicationPortOutViolationInterface
  }
}
