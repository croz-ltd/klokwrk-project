package org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.valid.adapter.in

import groovy.transform.CompileStatic
import org.klokwrk.lib.archunit.samplepackages.architecture.hexagonal.cqrses.valid.application.port.in.ApplicationPortInInterface

@SuppressWarnings('unused')
@CompileStatic
class AdapterInClass implements ApplicationPortInInterface {
  ApplicationPortInInterface applicationPortInInterface

  AdapterInClass(ApplicationPortInInterface applicationPortInInterface) {
    this.applicationPortInInterface = applicationPortInInterface
  }
}
