package org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.valid.adapter.projection

import groovy.transform.CompileStatic
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.valid.domain.event.DomainEventClass
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.valid.domain.model.DomainModelClass

@SuppressWarnings('unused')
@CompileStatic
class AdapterProjectionClass {
  DomainModelClass domainModelClass = new DomainModelClass()
  DomainEventClass domainEventClass = new DomainEventClass()
}
