/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracker.booking.domain.model.aggregate

import groovy.transform.CompileStatic
import org.klokwrk.cargotracking.domain.model.value.Cargo
import org.klokwrk.cargotracking.domain.model.value.CommodityType
import org.klokwrk.cargotracking.domain.model.value.ContainerType
import org.klokwrk.lib.xlang.groovy.base.transform.KwrkImmutable

import javax.measure.Quantity
import javax.measure.quantity.Temperature

/**
 * Encapsulates properties of a cargo used for cargo equality comparisons at the level of booking offer.
 */
@CompileStatic
@KwrkImmutable(knownImmutableClasses = [Quantity])
 class BookingOfferCargoEquality {
  CommodityType commodityType
  ContainerType containerType
  Quantity<Temperature> commodityRequestedStorageTemperature

  static BookingOfferCargoEquality fromCargo(Cargo cargo) {
    BookingOfferCargoEquality bookingOfferCargoMapKey = new BookingOfferCargoEquality(
        commodityType: cargo.commodity.commodityType,
        containerType: cargo.containerType,
        commodityRequestedStorageTemperature: cargo.commodity.requestedStorageTemperature
    )

    return bookingOfferCargoMapKey
  }
}
