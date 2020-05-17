package org.klokwrk.cargotracker.booking.commandside.cargobook.interfaces.web

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.commandside.cargobook.boundary.CargoBookRequest

// In general, in web interface we use CargoBookRequest directly, but CargoBookWebRequest can be used for adding additional properties that are only web specific and handled in
// controller before sending CargoBookRequest into domain facade.
@CompileStatic
class CargoBookWebRequest extends CargoBookRequest {
}
