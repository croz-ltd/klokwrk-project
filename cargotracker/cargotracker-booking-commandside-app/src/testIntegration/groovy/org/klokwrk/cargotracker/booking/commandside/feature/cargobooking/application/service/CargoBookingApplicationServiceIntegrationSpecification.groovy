package org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.service

import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in.BookCargoPortIn
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in.BookCargoRequest
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in.BookCargoResponse
import org.klokwrk.cargotracker.booking.commandside.test.base.AbstractCommandSideIntegrationSpecification
import org.klokwrk.cargotracker.booking.commandside.test.fixtures.metadata.WebMetaDataFixtures
import org.klokwrk.cargotracker.lib.boundary.api.operation.OperationRequest
import org.klokwrk.cargotracker.lib.boundary.api.operation.OperationResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("testIntegration")
class CargoBookingApplicationServiceIntegrationSpecification extends AbstractCommandSideIntegrationSpecification {
  @Autowired
  BookCargoPortIn bookCargoPortIn

  void "should work for correct request"() {
    given:
    String myAggregateIdentifier = UUID.randomUUID()
    BookCargoRequest bookCargoRequest = new BookCargoRequest(aggregateIdentifier: myAggregateIdentifier, originLocation: "HRZAG", destinationLocation: "HRRJK")
    Map requestMetadataMap = WebMetaDataFixtures.metaDataMapForWebBookingChannel()

    when:
    OperationResponse<BookCargoResponse> bookCargoOperationResponse = bookCargoPortIn.bookCargo(new OperationRequest<>(payload: bookCargoRequest, metaData: requestMetadataMap))
    BookCargoResponse bookCargoResponsePayload = bookCargoOperationResponse.payload
    Map bookCargoResponseMetadata = bookCargoOperationResponse.metaData

    then:
    bookCargoResponseMetadata.isEmpty()
    verifyAll(bookCargoResponsePayload) {
      aggregateIdentifier == myAggregateIdentifier
      originLocation.name == "Zagreb"
      destinationLocation.name == "Rijeka"
    }
  }
}
