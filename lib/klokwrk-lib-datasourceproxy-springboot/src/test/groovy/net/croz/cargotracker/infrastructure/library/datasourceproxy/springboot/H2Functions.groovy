package net.croz.cargotracker.infrastructure.library.datasourceproxy.springboot

import groovy.transform.CompileStatic

@CompileStatic
class H2Functions {

  @SuppressWarnings("unused")
  static Integer sleep(long milliSeconds) throws Exception {
    Thread.sleep(milliSeconds)
    return milliSeconds
  }
}
