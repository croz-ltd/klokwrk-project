package org.klokwrk.cargotracker.booking.commandside.cargobook.domain.facade

import org.klokwrk.cargotracker.booking.commandside.cargobook.test.base.AbstractCargoBookIntegrationSpecification
import org.klokwrk.cargotracker.booking.commandside.test.fixtures.metadata.WebMetaDataFixtures
import org.klokwrk.cargotracker.lib.boundary.api.operation.OperationRequest
import org.klokwrk.cargotracker.lib.boundary.api.operation.OperationResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class CargoBookFacadeServiceIntegrationSpecification extends AbstractCargoBookIntegrationSpecification {
  @Autowired
  CargoBookFacadeService cargoBookFacadeService

  void "should work for correct request"() {
    given:
    String myAggregateIdentifier = UUID.randomUUID()
    CargoBookRequest cargoBookRequest = new CargoBookRequest(aggregateIdentifier: myAggregateIdentifier, originLocation: "HRZAG", destinationLocation: "HRRJK")
    Map requestMetadataMap = WebMetaDataFixtures.metaDataMapForWebBookingChannel()

    when:
    OperationResponse<CargoBookResponse> cargoBookOperationResponse = cargoBookFacadeService.cargoBook(new OperationRequest<>(payload: cargoBookRequest, metaData: requestMetadataMap))
    CargoBookResponse cargoBookResponsePayload = cargoBookOperationResponse.payload
    Map cargoBookResponseMetadata = cargoBookOperationResponse.metaData

    then:
    cargoBookResponseMetadata.isEmpty()
    verifyAll(cargoBookResponsePayload) {
      aggregateIdentifier == myAggregateIdentifier
      originLocation.name == "Zagreb"
      destinationLocation.name == "Rijeka"
    }
  }
}
