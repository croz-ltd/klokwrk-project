package org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.adapter.in.web

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in.BookCargoRequest

/**
 * Represents a web request for book cargo operation.
 * <p/>
 * Very often, web interface will use <code>BookCargoRequest</code> directly. However, <code>BookCargoWebRequest</code> can be used for handling additional properties that are only web specific and
 * should be handled in controller before sending the <code>BookCargoRequest</code> into domain application layer.
 */
@CompileStatic
class BookCargoWebRequest extends BookCargoRequest {
}
