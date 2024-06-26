/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2024 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracking.booking.app.queryside.view.feature.bookingoffer.application.port.in

import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import org.klokwrk.cargotracking.domain.model.value.CommodityType
import org.klokwrk.cargotracking.domain.model.value.CustomerType
import org.klokwrk.lib.xlang.groovy.base.transform.options.RelaxedPropertyHandler

import javax.measure.Quantity
import javax.measure.quantity.Mass
import java.time.Instant

@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@MapConstructor(noArg = true)
@CompileStatic
class BookingOfferSummaryFindByIdQueryResponse {
  String bookingOfferId

  CustomerType customerType

  String originLocationUnLoCode
  String originLocationName
  String originLocationCountryName

  String destinationLocationUnLoCode
  String destinationLocationName
  String destinationLocationCountryName

  Instant departureEarliestTime
  Instant departureLatestTime
  Instant arrivalLatestTime

  Set<CommodityType> commodityTypes
  Quantity<Mass> totalCommodityWeight
  BigDecimal totalContainerTeuCount

  Instant firstEventRecordedAt
  Instant lastEventRecordedAt
  Long lastEventSequenceNumber
}
