package org.klokwrk.cargotracker.booking.commandside.test.fixtures

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.boundary.web.metadata.WebMetaDataFactory

@CompileStatic
class WebMetaDataFixtures {
  static Map<String, ?> metaDataMapForWebBookingChannel() {
    return WebMetaDataFactory.createMetaDataMapForWebBookingChannel("127.0.0.1")
  }
}
