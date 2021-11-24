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
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in.BookCargoCommandRequest
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.in.BookCargoCommandResponse
import org.klokwrk.cargotracker.booking.commandside.feature.cargobooking.application.port.out.LocationByUnLoCodeQueryPortOut
import org.klokwrk.cargotracker.booking.domain.model.value.Location
import org.klokwrk.cargotracker.lib.boundary.api.exception.CommandException
import org.klokwrk.cargotracker.lib.boundary.api.violation.ViolationInfo
import org.springframework.stereotype.Service

import static org.hamcrest.Matchers.notNullValue

/**
 * Convenient factory intended to be used by {@link CargoBookingApplicationService} for encapsulating and simplifying creation of objects required in {@link CargoBookingApplicationService}
 * implementation.
 */
@Service
@CompileStatic
class CargoBookingFactoryService {
  private final LocationByUnLoCodeQueryPortOut locationByUnLoCodeQueryPortOut

  CargoBookingFactoryService(LocationByUnLoCodeQueryPortOut locationByUnLoCodeQueryPortOut) {
    this.locationByUnLoCodeQueryPortOut = locationByUnLoCodeQueryPortOut
  }

  /**
   * Creates {@link BookCargoCommand} from supplied {@link BookCargoCommandRequest} instance.
   */
  BookCargoCommand createBookCargoCommand(BookCargoCommandRequest bookCargoCommandRequest) {
    requireMatch(bookCargoCommandRequest, notNullValue())

    String aggregateIdentifier = bookCargoCommandRequest.aggregateIdentifier ?: UUID.randomUUID().toString()
    requireMatch(UUID.fromString(aggregateIdentifier), notNullValue())

    // NOTE: While creating a command, we also need to resolve all required data from external services if needed. In this example, we are resolving registry data (a.k.a. master data) from the
    //       outbound adapter.
    //
    //       Data and objects comprising a command should be in their fully valid state, and it is best when involved data pieces are immutable objects.
    //
    //       Alternatively, this validation can be implemented via bean validation library. Although this provides nice possibility of matching the violation with corresponding property in the
    //       request (if this makes sense), there are some disadvantages.
    //
    //       First, here we have a validation dependent on the system state (external registry), which is not an ideal fit for bean validation (access to the system state will require an injection
    //       of additional resources). We should primarily use bean validation for syntax level validations with minimal business logic and without requirements for the system state.
    //
    //       Validation depending on the system state, is more suitable for implementing in actual business layers. We should implement the preparation and resolve of data in the domain facade during
    //       command construction. Any other kind of more involved validation should usually go in the aggregate.
    //
    //       Further, the validator and its corresponding annotation will be highly domain and use-case specific which will tie them to the domain facade/application layer. Also, data resolving
    //       should rarely fail as original unresolved data should be provided as a selectable UI choice (populated with registry data fetched from backend) instead of a free-form entry.
    Location originLocation = locationByUnLoCodeQueryPortOut.locationByUnLoCodeQuery(bookCargoCommandRequest.originLocation)
    requireKnownLocation(originLocation, "originLocationUnknown")

    Location destinationLocation = locationByUnLoCodeQueryPortOut.locationByUnLoCodeQuery(bookCargoCommandRequest.destinationLocation)
    requireKnownLocation(destinationLocation, "destinationLocationUnknown")

    BookCargoCommand bookCargoCommand = new BookCargoCommand(aggregateIdentifier: aggregateIdentifier, originLocation: originLocation, destinationLocation: destinationLocation)
    return bookCargoCommand
  }

  protected void requireKnownLocation(Location location, String violationCodeKey) {
    if (location == Location.UNKNOWN_LOCATION) {
      throw new CommandException(ViolationInfo.createForBadRequestWithCustomCodeKey(violationCodeKey))
    }
  }

  /**
   * Creates {@link BookCargoCommandResponse} from supplied {@link CargoAggregate} instance.
   */
  BookCargoCommandResponse createBookCargoCommandResponse(CargoAggregate cargoAggregate) {
    requireMatch(cargoAggregate, notNullValue())

    // NOTE: A direct synchronous returning of aggregate state, as is done here, is considered an anti-pattern and should be avoided in general. However, there are situations where it might be
    //       helpful, but please consider best-practice alternatives first, like using queryside or subscription queries.
    //
    //       Remember that you may not need data from the server at all (except acknowledge that command is accepted, i.e., HTTP OK status). All data sent via command are correct if the command has
    //       been accepted. In some situations, this may be enough for the client.
    //
    //       Use direct synchronous returning of aggregate state only as a last measure.
    //
    //       The internal aggregate state is not suitable for exposure to clients for multiple reasons like unwanted coupling and security. Moreover, even if the coupling is not a problem (i.e., when
    //       the same team implements clients and aggregates), such API will be highly unstable. We might remedy that instability somewhat by applying transformations as done here.
    //
    //       An additional problem is a tendency to add fields not required by business logic in the aggregate but only for display purposes.
    //
    //       If clients still decide to use direct responses from aggregate for gaining data, they should be aware of consequences and have a robust suite of regression tests in place.
    Map<String, ?> originLocationMap = createMapFromLocation(cargoAggregate.originLocation)
    Map<String, ?> destinationLocationMap = createMapFromLocation(cargoAggregate.destinationLocation)

    BookCargoCommandResponse bookCargoCommandResponse =
        new BookCargoCommandResponse(aggregateIdentifier: cargoAggregate.aggregateIdentifier, originLocation: originLocationMap, destinationLocation: destinationLocationMap)

    return bookCargoCommandResponse
  }

  /**
   * Creates, or "render" a map from {@link Location} instance.
   */
  protected Map<String, ?> createMapFromLocation(Location location) {
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
        ]
    ]

    return renderedMap
  }
}
