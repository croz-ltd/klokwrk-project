package org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.valid.application.port.out

import groovy.transform.CompileStatic
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.valid.domain.model.DomainModelClass

@SuppressWarnings('unused')
@CompileStatic
interface ApplicationPortOutInterface {
  void accessDomainModelClass(DomainModelClass domainModelClass)
}
