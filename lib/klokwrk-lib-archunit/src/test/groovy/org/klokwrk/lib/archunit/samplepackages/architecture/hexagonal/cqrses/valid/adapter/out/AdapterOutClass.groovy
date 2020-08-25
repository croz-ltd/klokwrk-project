package org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.valid.adapter.out

import groovy.transform.CompileStatic
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.valid.application.port.out.ApplicationPortOutInterface
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.valid.domain.model.DomainModelClass

@SuppressWarnings('unused')
@CompileStatic
class AdapterOutClass implements ApplicationPortOutInterface {
  DomainModelClass domainModelClass = new DomainModelClass()

  @Override
  void accessDomainModelClass(DomainModelClass domainModelClass) {
  }
}
