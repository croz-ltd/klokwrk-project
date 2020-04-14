package net.croz.cargotracker.booking.commandside.interfaces.web.conversation

import groovy.transform.CompileStatic
import net.croz.cargotracker.booking.api.open.commandside.conversation.CargoBookRequest

// In general, in web interface we can use only CargoBookRequest, but CargoBookWebRequest can be used for adding additional properties that are only web specific.
@CompileStatic
class CargoBookWebRequest extends CargoBookRequest {
}
