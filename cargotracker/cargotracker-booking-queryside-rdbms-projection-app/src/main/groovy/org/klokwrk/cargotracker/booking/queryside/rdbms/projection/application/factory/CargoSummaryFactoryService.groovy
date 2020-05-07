package org.klokwrk.cargotracker.booking.queryside.rdbms.projection.application.factory

import groovy.transform.CompileStatic
import net.croz.cargotracker.infrastructure.project.boundary.api.conversation.metadata.MetaDataConstant
import org.axonframework.eventhandling.DomainEventMessage
import org.klokwrk.cargotracker.booking.axon.api.event.CargoBookedEvent
import org.klokwrk.cargotracker.booking.queryside.rdbms.domain.querymodel.CargoSummaryQueryEntity

import static net.croz.cargotracker.lang.groovy.constant.CommonConstants.NOT_AVAILABLE

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
        inboundChannelName: domainEventMessage.metaData[MetaDataConstant.INBOUND_CHANNEL_NAME_KEY] ?: NOT_AVAILABLE,
        inboundChannelType: domainEventMessage.metaData[MetaDataConstant.INBOUND_CHANNEL_TYPE_KEY] ?: NOT_AVAILABLE
    )

    return cargoSummaryQueryEntity
  }
}
