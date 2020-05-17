package org.klokwrk.cargotracker.booking.commandside.cargobook.interfaces.web

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.commandside.cargobook.boundary.CargoBookRequest
import org.klokwrk.cargotracker.lib.boundary.api.conversation.OperationRequest
import org.klokwrk.cargotracker.lib.boundary.api.conversation.metadata.MetaDataConstant

import javax.servlet.http.HttpServletRequest

// Not really needed since implementation is very simple and be easily inlined into controller. However, it demonstrates where web-to-facade assemblers should live and how to operate.
@CompileStatic
class CargoBookCommandAssembler {
  static OperationRequest<CargoBookRequest> toCargoBookRequest(CargoBookWebRequest cargoBookWebRequest, HttpServletRequest httpServletRequest) {
    Map metadataMap = [
        (MetaDataConstant.INBOUND_CHANNEL_NAME_KEY): "booking",
        (MetaDataConstant.INBOUND_CHANNEL_TYPE_KEY): "web",
        // TODO dmurat: insert here more elaborate mechanism for detecting client IP. Good reference: https://www.marcobehler.com/guides/spring-mvc#_how_to_get_the_users_ip_address
        (MetaDataConstant.INBOUND_CHANNEL_REQUEST_IDENTIFIER_KEY): httpServletRequest.remoteAddr
    ]

    return new OperationRequest(payload: new CargoBookRequest(cargoBookWebRequest.properties), metaData: metadataMap)
  }
}
