package org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.application.service

import groovy.transform.CompileStatic
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.adapter.in.AdapterInViolationClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.adapter.out.AdapterOutViolationClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.adapter.projection.AdapterProjectionViolationClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.application.port.in.ApplicationPortInViolationInterface
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.application.port.out.ApplicationPortOutViolationInterface
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.domain.aggregate.DomainAggregateViolationClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.domain.command.DomainCommandViolationClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.domain.event.DomainEventViolationClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.violation.domain.model.DomainModelViolationClass

@SuppressWarnings('unused')
@CompileStatic
class ApplicationServiceViolationClass implements ApplicationPortInViolationInterface {
  DomainEventViolationClass domainEventViolationClass = new DomainEventViolationClass()

  @Override
  void accessDomainModelClass(DomainModelViolationClass domainModelViolationClass) {
  }

  @Override
  void accessDomainEventClass(DomainEventViolationClass domainEventViolationClass) {
  }

  @Override
  void accessDomainCommandClass(DomainCommandViolationClass domainCommandViolationClass) {
  }

  @Override
  void accessDomainAggregateClass(DomainAggregateViolationClass domainAggregateViolationClass) {
  }

  @Override
  void accessApplicationPortOut(ApplicationPortOutViolationInterface applicationPortOutViolationInterface) {
  }

  @Override
  void accessApplicationService(ApplicationServiceViolationClass applicationServiceViolationClass) {
  }

  @Override
  void accessAdapterIn(AdapterInViolationClass adapterInViolationClass) {
  }

  @Override
  void accessAdapterOut(AdapterOutViolationClass adapterOutViolationClass) {
  }

  @Override
  void accessAdapterProjection(AdapterProjectionViolationClass adapterProjectionViolationClass) {
  }
}
