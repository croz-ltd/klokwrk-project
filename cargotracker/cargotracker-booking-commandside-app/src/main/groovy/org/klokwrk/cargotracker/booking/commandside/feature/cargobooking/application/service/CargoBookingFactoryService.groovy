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
package org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.service

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in.CreateBookingOfferCommandRequest
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in.CreateBookingOfferCommandResponse
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.out.LocationByUnLoCodeQueryPortOut
import org.klokwrk.cargotracker.booking.domain.model.aggregate.BookingOfferAggregate
import org.klokwrk.cargotracker.booking.domain.model.command.CreateBookingOfferCommand
import org.klokwrk.cargotracker.booking.domain.model.value.BookingOfferId
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityInfo
import org.klokwrk.cargotracker.booking.domain.model.value.Location
import org.klokwrk.cargotracker.booking.domain.model.value.PortCapabilityType
import org.klokwrk.cargotracker.booking.domain.model.value.RouteSpecification
import org.springframework.stereotype.Service

import java.time.Clock

import static org.hamcrest.Matchers.notNullValue

/**
 * Convenient factory intended to be used by {@link CargoBookingApplicationService} for encapsulating and simplifying creation of objects required in {@link CargoBookingApplicationService}
 * implementation.
 */
@Service
@CompileStatic
class CargoBookingFactoryService {
  private final LocationByUnLoCodeQueryPortOut locationByUnLoCodeQueryPortOut
  private final Clock clock

  @SuppressWarnings("CodeNarc.OptionalMethodParameter")
  CargoBookingFactoryService(LocationByUnLoCodeQueryPortOut locationByUnLoCodeQueryPortOut, Optional<Clock> clockOptional) {
    this.locationByUnLoCodeQueryPortOut = locationByUnLoCodeQueryPortOut
    this.clock = clockOptional.orElse(Clock.systemUTC())
  }

  /**
   * Creates {@link CreateBookingOfferCommand} from supplied {@link CreateBookingOfferCommandRequest} instance.
   */
  CreateBookingOfferCommand makeCreateBookingOfferCommand(CreateBookingOfferCommandRequest createBookingOfferCommandRequest) {
    requireMatch(createBookingOfferCommandRequest, notNullValue())

    // NOTE: Since commands are immutable objects, the command's data and objects should be in their fully valid state after the command is constructed.
    //       While creating a command, we sometimes have to resolve data from external services. The domain facade is an excellent choice for such activities. In this example, we resolve Location
    //       registry data (a.k.a. master data) from the outbound adapter.

    Location resolvedOriginLocation = locationByUnLoCodeQueryPortOut.locationByUnLoCodeQuery(createBookingOfferCommandRequest.routeSpecification.originLocation)
    Location resolvedDestinationLocation = locationByUnLoCodeQueryPortOut.locationByUnLoCodeQuery(createBookingOfferCommandRequest.routeSpecification.destinationLocation)

    CreateBookingOfferCommand createBookingOfferCommand = new CreateBookingOfferCommand(
        bookingOfferId: BookingOfferId.makeWithGeneratedIdentifierIfNeeded(createBookingOfferCommandRequest.bookingOfferIdentifier),
        routeSpecification: RouteSpecification.make(
            resolvedOriginLocation, resolvedDestinationLocation,
            createBookingOfferCommandRequest.routeSpecification.departureEarliestTime, createBookingOfferCommandRequest.routeSpecification.departureLatestTime,
            createBookingOfferCommandRequest.routeSpecification.arrivalLatestTime, clock
        ),
        commodityInfo: CommodityInfo.make(
            createBookingOfferCommandRequest.commodityInfo.commodityType, createBookingOfferCommandRequest.commodityInfo.totalWeightInKilograms,
            createBookingOfferCommandRequest.commodityInfo.requestedStorageTemperatureInCelsius
        ),
        containerDimensionType: createBookingOfferCommandRequest.containerDimensionType
    )

    return createBookingOfferCommand
  }

  /**
   * Creates {@link CreateBookingOfferCommandResponse} from supplied {@link BookingOfferAggregate} instance.
   */
  CreateBookingOfferCommandResponse makeCreateBookingOfferCommandResponse(BookingOfferAggregate bookingOfferAggregate) {
    requireMatch(bookingOfferAggregate, notNullValue())

    // NOTE: A direct synchronous returning of aggregate state, as is done here, is considered an anti-pattern and should be avoided in general. However, there are situations where it might be
    //       helpful, but please consider best-practice alternatives first, like using queryside or subscription queries.
    //
    //       Remember that you may not need data from the server at all (except acknowledge that command is accepted, i.e., HTTP OK status). All data sent via command are correct if the command has
    //       been accepted. In some situations, this may be enough for the client.
    //
    //       Use direct synchronous returning of aggregate state only as a last measure.
    //
    //       The internal aggregate state is not suitable for exposure to clients for multiple reasons like unwanted coupling and security. Moreover, even if the coupling is not a problem (i.e., when
    //       the same team implements clients and aggregates), such API will be highly unstable. We might remedy that instability somewhat by applying custom transformations as done here.
    //       Don't concentrate too much on actual transformation and what it produces in this concrete example. It can be anything.
    //
    //       An additional problem is a tendency to add fields not required by business logic in the aggregate but only for display purposes.
    //
    //       If clients still decide to use direct responses from aggregate for gaining data, they should be aware of consequences and have a robust suite of regression tests in place.

    Map<String, ?> originLocationMap = makeMapFromLocation(bookingOfferAggregate.routeSpecification.originLocation)
    Map<String, ?> destinationLocationMap = makeMapFromLocation(bookingOfferAggregate.routeSpecification.destinationLocation)

    CreateBookingOfferCommandResponse createBookingOfferCommandResponse = new CreateBookingOfferCommandResponse(
        bookingOfferId: [identifier: bookingOfferAggregate.bookingOfferId.identifier],
        routeSpecification: [
            originLocation: originLocationMap, destinationLocation: destinationLocationMap,
            departureEarliestTime: bookingOfferAggregate.routeSpecification.departureEarliestTime, departureLatestTime: bookingOfferAggregate.routeSpecification.departureLatestTime,
            arrivalLatestTime: bookingOfferAggregate.routeSpecification.arrivalLatestTime
        ],
        bookingOfferCommodities: [
            commodityTypeToCommodityMap: bookingOfferAggregate.bookingOfferCommodities.commodityTypeToCommodityMap,
            totalCommodityWeight: bookingOfferAggregate.bookingOfferCommodities.totalCommodityWeight,
            totalContainerCount: bookingOfferAggregate.bookingOfferCommodities.totalContainerCount
        ]
    )

    return createBookingOfferCommandResponse
  }

  /**
   * Creates, or "render" a map from {@link Location} instance.
   */
  protected Map<String, ?> makeMapFromLocation(Location location) {
    Map<String, ?> renderedMap = [
        name: location.name.nameInternationalized,
        countryName: location.countryName.nameInternationalized,
        unLoCode: [
            code: [
                encoded: location.unLoCode.code,
                countryCode: location.unLoCode.countryCode,
                locationCode: location.unLoCode.locationCode
            ],
            coordinates: [
                encoded: location.unLoCodeCoordinates.coordinatesEncoded,
                latitudeInDegrees: location.unLoCodeCoordinates.latitudeInDegrees,
                longitudeInDegrees: location.unLoCodeCoordinates.longitudeInDegrees
            ],
            function: [
                encoded: location.unLoCodeFunction.functionEncoded,
                isPort: location.unLoCodeFunction.port,
                isRailTerminal: location.unLoCodeFunction.railTerminal,
                isRoadTerminal: location.unLoCodeFunction.roadTerminal,
                isAirport: location.unLoCodeFunction.airport,
                isPostalExchangeOffice: location.unLoCodeFunction.postalExchangeOffice,
                isBorderCrossing: location.unLoCodeFunction.borderCrossing,
            ]
        ],
        portCapabilities: location.portCapabilities.capabilities.sort(false, { PortCapabilityType portCapabilityType -> portCapabilityType.name() }) as String[]
    ]

    return renderedMap
  }
}
