package org.klokwrk.cargotracker.booking.commandside.cargobook.interfaces.web

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.commandside.cargobook.domain.facade.CargoBookRequest
import org.klokwrk.cargotracker.lib.boundary.api.metadata.constant.MetaDataConstant
import org.klokwrk.cargotracker.lib.boundary.api.operation.OperationRequest

import javax.servlet.http.HttpServletRequest

// Not really needed since implementation is very simple and be easily inlined into controller. However, it demonstrates where web-to-facade assemblers should live and how to operate.
@CompileStatic
class CargoBookCommandAssembler {
  static final String WEB_BOOKING_CHANNEL_NAME = "booking"
  static final String WEB_BOOKING_CHANNEL_TYPE = "web"

  static OperationRequest<CargoBookRequest> toCargoBookRequest(CargoBookWebRequest cargoBookWebRequest, HttpServletRequest httpServletRequest) {
    // TODO dmurat: insert here more elaborate mechanism for detecting client IP. Good reference: https://www.marcobehler.com/guides/spring-mvc#_how_to_get_the_users_ip_address
    Map metadataMap = createMetaDataMapForWebBookingChannel(httpServletRequest.remoteAddr)

    return new OperationRequest(payload: new CargoBookRequest(cargoBookWebRequest.properties), metaData: metadataMap)
  }

  /**
   * Creates a simple map representing meta data for <code>web-booking</code> inbound channel.
   * <p/>
   * Parameter <code>inboundChannelRequestIdentifier</code> should specify unique identifier of web request, i.e. IP address of remote user.
   */
  static Map<String, ?> createMetaDataMapForWebBookingChannel(String inboundChannelRequestIdentifier) {
    Map metadataMap = [
        (MetaDataConstant.INBOUND_CHANNEL_NAME_KEY): WEB_BOOKING_CHANNEL_NAME,
        (MetaDataConstant.INBOUND_CHANNEL_TYPE_KEY): WEB_BOOKING_CHANNEL_TYPE,
        (MetaDataConstant.INBOUND_CHANNEL_REQUEST_IDENTIFIER_KEY): inboundChannelRequestIdentifier
    ]

    return metadataMap
  }
}
