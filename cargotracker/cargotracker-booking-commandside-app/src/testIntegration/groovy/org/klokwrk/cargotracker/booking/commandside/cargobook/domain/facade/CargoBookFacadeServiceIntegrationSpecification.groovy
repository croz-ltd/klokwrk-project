package org.klokwrk.cargotracker.booking.commandside.cargobook.domain.facade

import org.klokwrk.cargotracker.booking.commandside.cargobook.test.base.AbstractCargoBookIntegrationSpecification
import org.klokwrk.cargotracker.lib.boundary.api.metadata.constant.MetaDataConstant
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
    Map requestMetadataMap = [
        (MetaDataConstant.INBOUND_CHANNEL_NAME_KEY): "booking",
        (MetaDataConstant.INBOUND_CHANNEL_TYPE_KEY): "web",
        (MetaDataConstant.INBOUND_CHANNEL_REQUEST_IDENTIFIER_KEY): "127.0.0.1"
    ]

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
