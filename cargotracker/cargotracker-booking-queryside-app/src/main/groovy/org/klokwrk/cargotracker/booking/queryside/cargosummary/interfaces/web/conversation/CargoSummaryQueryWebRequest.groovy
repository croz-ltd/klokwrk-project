package org.klokwrk.cargotracker.booking.queryside.cargosummary.interfaces.web.conversation

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.queryside.cargosummary.boundary.api.conversation.CargoSummaryQueryRequest

// In general, in web interface we can use only CargoSummaryQueryRequest, but CargoSummaryQueryWebRequest can be used for adding additional properties that are only web specific.
@CompileStatic
class CargoSummaryQueryWebRequest extends CargoSummaryQueryRequest {
}
