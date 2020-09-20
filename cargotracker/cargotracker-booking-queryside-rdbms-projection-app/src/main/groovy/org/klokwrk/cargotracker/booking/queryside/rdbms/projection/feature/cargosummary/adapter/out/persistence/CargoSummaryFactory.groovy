package org.klokwrk.cargotracker.booking.queryside.rdbms.projection.feature.cargosummary.adapter.out.persistence

import groovy.transform.CompileStatic
import org.axonframework.eventhandling.DomainEventMessage
import org.klokwrk.cargotracker.booking.axon.api.feature.cargobooking.event.CargoBookedEvent
import org.klokwrk.cargotracker.booking.queryside.rdbms.projection.model.CargoSummaryJpaEntity
import org.klokwrk.cargotracker.lib.boundary.api.metadata.constant.MetaDataConstant
import org.klokwrk.lang.groovy.constant.CommonConstants

@CompileStatic
class CargoSummaryFactory {
  static CargoSummaryJpaEntity createCargoSummaryJpaEntity(CargoBookedEvent cargoBookedEvent, DomainEventMessage domainEventMessage) {
    // TODO dmurat: automate populating persistent entity
    String aggregateIdentifier = cargoBookedEvent.aggregateIdentifier
    String originLocation = cargoBookedEvent.originLocation.unLoCode.code
    String destinationLocation = cargoBookedEvent.destinationLocation.unLoCode.code
    Long aggregateSequenceNumber = domainEventMessage.sequenceNumber

    CargoSummaryJpaEntity cargoSummaryJpaEntity = new CargoSummaryJpaEntity(
      aggregateIdentifier: aggregateIdentifier, aggregateSequenceNumber: aggregateSequenceNumber, originLocation: originLocation, destinationLocation: destinationLocation,
      inboundChannelName: domainEventMessage.metaData[MetaDataConstant.INBOUND_CHANNEL_NAME_KEY] ?: CommonConstants.NOT_AVAILABLE,
      inboundChannelType: domainEventMessage.metaData[MetaDataConstant.INBOUND_CHANNEL_TYPE_KEY] ?: CommonConstants.NOT_AVAILABLE
    )

    return cargoSummaryJpaEntity
  }
}
