package net.croz.cargotracker.booking.queryside.interfaces.web.dto

import net.croz.cargotracker.booking.api.open.queryside.conversation.CargoSummaryQueryRequest

// In general, in web interface we can use only CargoSummaryQueryRequest, but CargoSummaryQueryWebRequest can be used for adding additional properties that are only web specific.
class CargoSummaryQueryWebRequest extends CargoSummaryQueryRequest {
}
