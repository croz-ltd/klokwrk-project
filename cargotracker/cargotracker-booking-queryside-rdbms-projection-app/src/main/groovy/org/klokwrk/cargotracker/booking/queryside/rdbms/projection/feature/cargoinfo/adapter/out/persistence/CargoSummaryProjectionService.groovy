/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracker.booking.queryside.rdbms.projection.feature.cargoinfo.adapter.out.persistence

import groovy.transform.CompileStatic
import org.axonframework.eventhandling.DomainEventMessage
import org.axonframework.eventhandling.EventHandler
import org.klokwrk.cargotracker.booking.domain.model.event.BookingOfferCreatedEvent
import org.klokwrk.cargotracker.booking.queryside.rdbms.projection.model.CargoSummaryJpaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
@CompileStatic
class CargoSummaryProjectionService {
  private final CargoSummaryJpaRepository cargoSummaryJpaRepository

  CargoSummaryProjectionService(CargoSummaryJpaRepository cargoSummaryJpaRepository) {
    this.cargoSummaryJpaRepository = cargoSummaryJpaRepository
  }

  @EventHandler
  void onCargoBookedEvent(BookingOfferCreatedEvent bookingOfferCreatedEvent, DomainEventMessage domainEventMessage) {
    cargoSummaryJpaRepository.save(CargoSummaryFactory.createCargoSummaryJpaEntity(bookingOfferCreatedEvent, domainEventMessage))
  }
}
