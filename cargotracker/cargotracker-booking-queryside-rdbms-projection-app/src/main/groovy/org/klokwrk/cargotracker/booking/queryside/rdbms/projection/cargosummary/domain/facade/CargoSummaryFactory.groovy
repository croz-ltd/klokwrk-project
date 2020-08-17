package org.klokwrk.cargotracker.booking.queryside.rdbms.projection.cargosummary.domain.facade

import groovy.transform.CompileStatic
import org.axonframework.eventhandling.DomainEventMessage
import org.klokwrk.cargotracker.booking.axon.api.feature.cargobooking.event.CargoBookedEvent
import org.klokwrk.cargotracker.booking.queryside.rdbms.projection.domain.querymodel.CargoSummaryQueryEntity
import org.klokwrk.cargotracker.lib.boundary.api.metadata.constant.MetaDataConstant

import static org.klokwrk.lang.groovy.constant.CommonConstants.NOT_AVAILABLE

@CompileStatic
class CargoSummaryFactory {
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
