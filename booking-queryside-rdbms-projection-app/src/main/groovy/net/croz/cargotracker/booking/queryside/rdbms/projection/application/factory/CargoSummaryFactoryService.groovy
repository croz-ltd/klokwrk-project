package net.croz.cargotracker.booking.queryside.rdbms.projection.application.factory

import groovy.transform.CompileStatic
import net.croz.cargotracker.api.open.shared.conversation.MetaDataConstant
import net.croz.cargotracker.booking.api.axon.event.CargoBookedEvent
import net.croz.cargotracker.booking.queryside.rdbms.domain.querymodel.CargoSummaryQueryEntity
import org.axonframework.eventhandling.DomainEventMessage

@CompileStatic
class CargoSummaryFactoryService {
  static CargoSummaryQueryEntity createCargoSummaryQueryEntity(CargoBookedEvent cargoBookedEvent, DomainEventMessage domainEventMessage) {
    // TODO dmurat: automate populating persistent entity
    String aggregateIdentifier = cargoBookedEvent.aggregateIdentifier
    String originLocation = cargoBookedEvent.originLocation.unLoCode.code
    String destinationLocation = cargoBookedEvent.destinationLocation.unLoCode.code
    Long aggregateSequenceNumber = domainEventMessage.sequenceNumber

    CargoSummaryQueryEntity cargoSummaryQueryEntity = new CargoSummaryQueryEntity(
        aggregateIdentifier: aggregateIdentifier, aggregateSequenceNumber: aggregateSequenceNumber, originLocation: originLocation, destinationLocation: destinationLocation,
        inboundChannelName: domainEventMessage.metaData[MetaDataConstant.INBOUND_CHANNEL_NAME_KEY] ?: "n/a",
        inboundChannelType: domainEventMessage.metaData[MetaDataConstant.INBOUND_CHANNEL_TYPE_KEY] ?: "n/a"
    )

    return cargoSummaryQueryEntity
  }
}
