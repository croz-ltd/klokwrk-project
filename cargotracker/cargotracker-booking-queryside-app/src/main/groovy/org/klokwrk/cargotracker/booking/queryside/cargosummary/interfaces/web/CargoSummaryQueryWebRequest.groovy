package org.klokwrk.cargotracker.booking.queryside.cargosummary.interfaces.web

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.queryside.cargosummary.boundary.CargoSummaryQueryRequest

// In general, in web interface we use CargoSummaryQueryRequest directly, but CargoSummaryQueryWebRequest can be used for adding additional properties that are only web specific and handled in
// controller before sending CargoSummaryQueryRequest into domain facade.
@CompileStatic
class CargoSummaryQueryWebRequest extends CargoSummaryQueryRequest {
}
