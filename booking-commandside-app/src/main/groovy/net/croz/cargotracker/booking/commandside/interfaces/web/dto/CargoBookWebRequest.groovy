package net.croz.cargotracker.booking.commandside.interfaces.web.dto

import net.croz.cargotracker.booking.commandside.conversation.CargoBookRequest

// In general, in web interface we can use only CargoBookRequest, but CargoBookWebRequest can be used for adding additional properties that are only web specific.
class CargoBookWebRequest extends CargoBookRequest {
}
