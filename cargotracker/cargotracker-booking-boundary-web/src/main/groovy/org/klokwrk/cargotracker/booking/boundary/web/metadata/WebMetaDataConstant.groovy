package org.klokwrk.cargotracker.booking.boundary.web.metadata

import groovy.transform.CompileStatic

@CompileStatic
class WebMetaDataConstant {
  /**
   * Represents a value for <code>MetaDataConstant.INBOUND_CHANNEL_NAME_KEY</code> when corresponding inbound request is directed via cargotracker booking web channel.
   */
  static final String WEB_BOOKING_CHANNEL_NAME = "booking"

  /**
   * Represents a value for <code>MetaDataConstant.INBOUND_CHANNEL_TYPE_KEY</code> when corresponding inbound request is directed via cargotracker booking web channel.
   */
  static final String WEB_BOOKING_CHANNEL_TYPE = "web"
}
