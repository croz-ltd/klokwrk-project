package org.klokwrk.cargotracker.booking.commandside.test.fixtures.metadata

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.boundary.web.metadata.WebMetaDataFactory

/**
 * Contains test metadata fixtures for <code>web-booking</code> inbound channel.
 */
@CompileStatic
class WebMetaDataFixtures {
  /**
   * Creates valid metadata for <code>web-booking</code> inbound channel.
   */
  static Map<String, ?> metaDataMapForWebBookingChannel() {
    return WebMetaDataFactory.createMetaDataMapForWebBookingChannel("127.0.0.1")
  }
}
