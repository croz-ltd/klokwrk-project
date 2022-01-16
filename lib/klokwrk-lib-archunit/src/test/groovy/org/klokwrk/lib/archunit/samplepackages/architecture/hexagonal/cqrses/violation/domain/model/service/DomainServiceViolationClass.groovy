package org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.domain.model.service

import groovy.transform.CompileStatic
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.adapter.in.AdapterInViolationClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.adapter.out.AdapterOutViolationClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.adapter.projection.AdapterProjectionViolationClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.application.port.in.ApplicationPortInViolationInterface
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.application.port.out.ApplicationPortOutViolationInterface
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.application.service.ApplicationServiceViolationClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.domain.model.aggregate.DomainAggregateViolationClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.domain.model.command.DomainCommandViolationClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.domain.model.event.DomainEventViolationClass

@SuppressWarnings('unused')
@CompileStatic
class DomainServiceViolationClass {
  DomainEventViolationClass domainEventViolationClass = new DomainEventViolationClass()
  DomainCommandViolationClass domainCommandViolationClass = new DomainCommandViolationClass()
  DomainAggregateViolationClass domainAggregateViolationClass = new DomainAggregateViolationClass()

  ApplicationPortInViolationInterface applicationPortInViolationInterface
  ApplicationPortOutViolationInterface applicationPortOutViolationInterface
  ApplicationServiceViolationClass applicationServiceViolationClass = new ApplicationServiceViolationClass()

  AdapterInViolationClass adapterInViolationClass = new AdapterInViolationClass()
  AdapterOutViolationClass adapterOutViolationClass = new AdapterOutViolationClass()
  AdapterProjectionViolationClass adapterProjectionViolationClass = new AdapterProjectionViolationClass()

  DomainServiceViolationClass() {
  }

  DomainServiceViolationClass(ApplicationPortInViolationInterface applicationPortInViolationInterface, ApplicationPortOutViolationInterface applicationPortOutViolationInterface) {
    this.applicationPortInViolationInterface = applicationPortInViolationInterface
    this.applicationPortOutViolationInterface = applicationPortOutViolationInterface
  }
}
