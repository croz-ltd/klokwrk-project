package org.klokwrk.cargotracker.booking.commandside.cargobook.interfaces.web.conversation

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.commandside.cargobook.boundary.api.conversation.CargoBookRequest

// In general, in web interface we can use only CargoBookRequest, but CargoBookWebRequest can be used for adding additional properties that are only web specific.
@CompileStatic
class CargoBookWebRequest extends CargoBookRequest {
}
