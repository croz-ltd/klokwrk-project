package org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.lib.boundary.api.operation.OperationRequest
import org.klokwrk.cargotracker.lib.boundary.api.operation.OperationResponse

@CompileStatic
interface BookCargoPortIn {
  OperationResponse<BookCargoResponse> bookCargo(OperationRequest<BookCargoRequest> bookCargoOperationRequest)
}
