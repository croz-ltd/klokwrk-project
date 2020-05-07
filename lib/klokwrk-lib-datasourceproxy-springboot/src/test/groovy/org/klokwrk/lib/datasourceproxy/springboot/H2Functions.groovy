package org.klokwrk.lib.datasourceproxy.springboot

import groovy.transform.CompileStatic

@SuppressWarnings("unused")
@CompileStatic
class H2Functions {

  @SuppressWarnings("unused")
  static Integer sleep(long milliSeconds) throws Exception {
    Thread.sleep(milliSeconds)
    return milliSeconds
  }
}
