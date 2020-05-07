package org.klokwrk.cargotracker.booking.commandside.interfaces.web.assembler

import groovy.transform.CompileStatic
import net.croz.cargotracker.booking.boundary.api.commandside.conversation.CargoBookRequest
import net.croz.cargotracker.infrastructure.project.boundary.api.conversation.OperationRequest
import net.croz.cargotracker.infrastructure.project.boundary.api.conversation.metadata.MetaDataConstant
import org.klokwrk.cargotracker.booking.commandside.interfaces.web.conversation.CargoBookWebRequest

import javax.servlet.http.HttpServletRequest

// Not really needed since implementation is very simple and be easily inlined into controller. However, it demonstrates where web-to-facade assemblers should live and how to operate.
@CompileStatic
class CargoBookingCommandAssembler {
  static OperationRequest<CargoBookRequest> toCargoBookRequest(CargoBookWebRequest cargoBookWebRequest, HttpServletRequest httpServletRequest) {
    Map metadataMap = [
        (MetaDataConstant.INBOUND_CHANNEL_NAME_KEY): "booking",
        (MetaDataConstant.INBOUND_CHANNEL_TYPE_KEY): "web",
        (MetaDataConstant.INBOUND_CHANNEL_REQUEST_IDENTIFIER_KEY): httpServletRequest.remoteAddr
    ]

    return new OperationRequest(payload: new CargoBookRequest(cargoBookWebRequest.properties), metaData: metadataMap)
  }
}
