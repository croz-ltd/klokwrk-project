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
package org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.service

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.axon.api.feature.cargobooking.command.BookCargoCommand
import org.klokwrk.cargotracker.booking.commandside.domain.aggregate.CargoAggregate
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in.BookCargoRequest
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in.BookCargoResponse
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.out.FindLocationPortOut
import org.klokwrk.cargotracker.booking.domain.model.Location
import org.springframework.stereotype.Service

import static org.hamcrest.Matchers.not
import static org.hamcrest.Matchers.notNullValue

/**
 * Convenient factory intended to be used by {@link CargoBookingApplicationService} for encapsulating and simplifying creation of objects required in {@link CargoBookingApplicationService}
 * implementation.
 */
@Service
@CompileStatic
class CargoBookingFactoryService {
  private final FindLocationPortOut findLocationPortOut

  CargoBookingFactoryService(FindLocationPortOut findLocationPortOut) {
    this.findLocationPortOut = findLocationPortOut
  }

  /**
   * Creates {@link BookCargoCommand} from supplied {@link org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in.BookCargoRequest} instance.
   */
  BookCargoCommand createBookCargoCommand(BookCargoRequest bookCargoRequest) {
    requireMatch(bookCargoRequest, notNullValue())

    String aggregateIdentifier = bookCargoRequest.aggregateIdentifier ?: UUID.randomUUID().toString()
    requireMatch(UUID.fromString(aggregateIdentifier), notNullValue())

    // NOTE: While creating a command, we also need to resolve all required data from external services if needed. In this example, we are resolving registry data (a.k.a. master data) from the
    //       outbound adapter.
    //       Data and objects comprising a command should be in their fully valid state and it is the best when involved data pieces are immutable objects.
    Location originLocation = findLocationPortOut.findByUnLoCode(bookCargoRequest.originLocation)
    // TODO dmurat: validation - replace with second level validation producing an exception that can be translated into meaningful response for the user.
    requireMatch(originLocation, not(Location.UNKNOWN_LOCATION))

    Location destinationLocation = findLocationPortOut.findByUnLoCode(bookCargoRequest.destinationLocation)
    // TODO dmurat: validation - replace with second level validation producing an exception that can be translated into meaningful response for the user.
    requireMatch(destinationLocation, not(Location.UNKNOWN_LOCATION))

    BookCargoCommand bookCargoCommand = new BookCargoCommand(aggregateIdentifier: aggregateIdentifier, originLocation: originLocation, destinationLocation: destinationLocation)
    return bookCargoCommand
  }

  /**
   * Creates {@link org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in.BookCargoResponse} from supplied {@link CargoAggregate} instance.
   */
  BookCargoResponse createBookCargoResponse(CargoAggregate cargoAggregate) {
    requireMatch(cargoAggregate, notNullValue())

    Map<String, ?> originLocationMap = createMapFromLocation(cargoAggregate.originLocation)
    Map<String, ?> destinationLocationMap = createMapFromLocation(cargoAggregate.destinationLocation)

    BookCargoResponse bookCargoResponse = new BookCargoResponse(aggregateIdentifier: cargoAggregate.aggregateIdentifier, originLocation: originLocationMap, destinationLocation: destinationLocationMap)
    return bookCargoResponse
  }

  /**
   * Creates, or "render" a map from {@link Location} instance.
   */
  protected Map<String, ?> createMapFromLocation(Location location) {
    Map<String, ?> renderedMap = [
        name: location.name.name,
        nameInternationalized: location.name.nameInternationalized,
        country: [
            name: location.countryName.name,
            nameInternationalized: location.countryName.nameInternationalized
        ],
        unLoCode: [
            code: location.unLoCode.code,
            countryCode: location.unLoCode.countryCode,
            locationCode: location.unLoCode.locationCode
        ]
    ]

    return renderedMap
  }
}
