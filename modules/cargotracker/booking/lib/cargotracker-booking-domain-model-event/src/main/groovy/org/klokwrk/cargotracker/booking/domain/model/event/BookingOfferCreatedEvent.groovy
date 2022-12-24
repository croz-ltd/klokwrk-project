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
package org.klokwrk.cargotracker.booking.domain.model.event

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.domain.model.event.data.CargoEventData
import org.klokwrk.cargotracker.booking.domain.model.event.data.CustomerEventData
import org.klokwrk.cargotracker.booking.domain.model.event.data.RouteSpecificationEventData
import org.klokwrk.cargotracker.lib.domain.model.event.BaseEvent
import org.klokwrk.lang.groovy.transform.KwrkImmutable

import javax.measure.Quantity
import javax.measure.quantity.Mass

@KwrkImmutable(knownImmutableClasses = [Quantity])
@CompileStatic
class BookingOfferCreatedEvent implements BaseEvent {
  String bookingOfferId
  CustomerEventData customer

  Collection<CargoEventData> cargos
  Quantity<Mass> totalCommodityWeight
  BigDecimal totalContainerTeuCount

  RouteSpecificationEventData routeSpecification

  String getAggregateIdentifier() {
    return bookingOfferId
  }
}
