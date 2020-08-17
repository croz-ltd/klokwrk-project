package org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.adapter.in.web

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.boundary.web.metadata.WebMetaDataFactory
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in.BookCargoRequest
import org.klokwrk.cargotracker.lib.boundary.api.operation.OperationRequest

import javax.servlet.http.HttpServletRequest

// Not really needed since implementation is very simple and be easily inlined into controller. However, it demonstrates where web-to-applicationLayer assemblers should live and how to operate.
@CompileStatic
class CargoBookingWebAssembler {

  static OperationRequest<BookCargoRequest> toBookCargoOperationRequest(BookCargoWebRequest bookCargoWebRequest, HttpServletRequest httpServletRequest) {
    // TODO dmurat: insert here more elaborate mechanism for detecting client IP. Good reference: https://www.marcobehler.com/guides/spring-mvc#_how_to_get_the_users_ip_address
    Map metadataMap = WebMetaDataFactory.createMetaDataMapForWebBookingChannel(httpServletRequest.remoteAddr)

    OperationRequest<BookCargoRequest> bookCargoOperationRequest = new OperationRequest(payload: new BookCargoRequest(bookCargoWebRequest.properties), metaData: metadataMap)
    return bookCargoOperationRequest
  }
}
