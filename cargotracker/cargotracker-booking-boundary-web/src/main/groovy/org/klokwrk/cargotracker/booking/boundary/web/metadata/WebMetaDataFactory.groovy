package org.klokwrk.cargotracker.booking.boundary.web.metadata

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.lib.boundary.api.metadata.constant.MetaDataConstant

@CompileStatic
class WebMetaDataFactory {

  /**
   * Creates a simple map representing meta data for <code>web-booking</code> inbound channel.
   * <p/>
   * Parameter <code>inboundChannelRequestIdentifier</code> should specify unique identifier of web request, i.e. IP address of remote user.
   */
  static Map<String, ?> createMetaDataMapForWebBookingChannel(String inboundChannelRequestIdentifier) {
    Map metadataMap = [
        (MetaDataConstant.INBOUND_CHANNEL_NAME_KEY): WebMetaDataConstant.WEB_BOOKING_CHANNEL_NAME,
        (MetaDataConstant.INBOUND_CHANNEL_TYPE_KEY): WebMetaDataConstant.WEB_BOOKING_CHANNEL_TYPE,
        (MetaDataConstant.INBOUND_CHANNEL_REQUEST_IDENTIFIER_KEY): inboundChannelRequestIdentifier
    ]

    return metadataMap
  }
}
