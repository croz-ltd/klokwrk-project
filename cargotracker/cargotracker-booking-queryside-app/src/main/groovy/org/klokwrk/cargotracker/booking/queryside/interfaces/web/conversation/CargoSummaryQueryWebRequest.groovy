package org.klokwrk.cargotracker.booking.queryside.interfaces.web.conversation

import groovy.transform.CompileStatic
import net.croz.cargotracker.booking.boundary.api.queryside.conversation.CargoSummaryQueryRequest

// In general, in web interface we can use only CargoSummaryQueryRequest, but CargoSummaryQueryWebRequest can be used for adding additional properties that are only web specific.
@CompileStatic
class CargoSummaryQueryWebRequest extends CargoSummaryQueryRequest {
}
