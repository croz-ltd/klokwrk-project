/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 CROZ d.o.o, the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
