package net.croz.cargotracker.booking.commandside.interfaces.web.assembler

import groovy.transform.CompileStatic
import net.croz.cargotracker.api.open.shared.conversation.OperationRequest
import net.croz.cargotracker.api.open.shared.conversation.OperationResponse
import net.croz.cargotracker.booking.api.open.commandside.conversation.CargoBookRequest
import net.croz.cargotracker.booking.api.open.commandside.conversation.CargoBookResponse
import net.croz.cargotracker.booking.commandside.interfaces.web.conversation.CargoBookWebRequest

// Not really needed since implementation is very simple and be easily inlined into controller. However, it demonstrates where web-to-facade assemblers should live and how to operate.
@CompileStatic
class CargoBookingCommandAssembler {
  static OperationRequest<CargoBookRequest> toCargoBookRequest(CargoBookWebRequest cargoBookWebRequest) {
    return new OperationRequest(payload: new CargoBookRequest(cargoBookWebRequest.properties))
  }

  static CargoBookResponse fromCargoBookResponse(OperationResponse<CargoBookResponse> cargoBookResponseOperationResponse) {
    return cargoBookResponseOperationResponse.payload
  }
}
