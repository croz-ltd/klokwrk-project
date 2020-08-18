package org.klokwrk.cargotracker.booking.queryside.feature.cargosummary.adapter.in.web

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.queryside.feature.cargosummary.application.port.in.FetchCargoSummaryQueryRequest

// In general, in web interface we use FetchCargoSummaryQueryRequest directly, but FetchCargoSummaryQueryWebRequest can be used for adding additional properties that are only web specific and handled in
// controller before sending FetchCargoSummaryQueryRequest into domain facade.
@CompileStatic
class FetchCargoSummaryQueryWebRequest extends FetchCargoSummaryQueryRequest {
}
