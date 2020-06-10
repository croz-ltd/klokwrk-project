package org.klokwrk.cargotracker.booking.commandside.cargobook.interfaces.web

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.commandside.cargobook.domain.facade.CargoBookRequest

/**
 * Represents a web request for booking cargo.
 * <p/>
 * In general, in web interface we will use <code>CargoBookRequest</code> directly, but <code>CargoBookWebRequest</code> can be used for adding and handling of additional properties that are only
 * web specific and should be handled in controller before sending the <code>CargoBookRequest</code> into domain facade.
 */
@CompileStatic
class CargoBookWebRequest extends CargoBookRequest {
}
