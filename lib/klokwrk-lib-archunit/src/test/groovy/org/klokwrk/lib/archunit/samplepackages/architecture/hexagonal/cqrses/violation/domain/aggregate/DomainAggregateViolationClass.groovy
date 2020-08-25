package org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.domain.aggregate

import groovy.transform.CompileStatic
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.adapter.in.AdapterInViolationClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.adapter.out.AdapterOutViolationClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.adapter.projection.AdapterProjectionViolationClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.application.port.in.ApplicationPortInViolationInterface
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.application.service.ApplicationServiceViolationClass

@SuppressWarnings('unused')
@CompileStatic
class DomainAggregateViolationClass {
  ApplicationPortInViolationInterface applicationPortInViolationInterface
  ApplicationServiceViolationClass applicationServiceViolationClass = new ApplicationServiceViolationClass()

  AdapterInViolationClass adapterInViolationClass = new AdapterInViolationClass()
  AdapterOutViolationClass adapterOutViolationClass = new AdapterOutViolationClass()
  AdapterProjectionViolationClass adapterProjectionViolationClass = new AdapterProjectionViolationClass()

  DomainAggregateViolationClass() {
  }

  DomainAggregateViolationClass(ApplicationPortInViolationInterface applicationPortInViolationInterface) {
    this.applicationPortInViolationInterface = applicationPortInViolationInterface
  }
}
