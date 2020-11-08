/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020 CROZ d.o.o, the original author or authors.
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
import org.axonframework.eventhandling.EventHandler
import org.klokwrk.cargotracker.booking.axon.api.feature.cargobooking.event.CargoBookedEvent
import org.klokwrk.cargotracker.booking.queryside.rdbms.projection.model.CargoSummaryJpaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
@CompileStatic
class CargoSummaryProjectorService {
  private final CargoSummaryJpaRepository cargoSummaryJpaRepository

  CargoSummaryProjectorService(CargoSummaryJpaRepository cargoSummaryJpaRepository) {
    this.cargoSummaryJpaRepository = cargoSummaryJpaRepository
  }

  @EventHandler
  void onCargoBookedEvent(CargoBookedEvent cargoBookedEvent, DomainEventMessage domainEventMessage) {
    cargoSummaryJpaRepository.save(CargoSummaryFactory.createCargoSummaryJpaEntity(cargoBookedEvent, domainEventMessage))
  }
}
