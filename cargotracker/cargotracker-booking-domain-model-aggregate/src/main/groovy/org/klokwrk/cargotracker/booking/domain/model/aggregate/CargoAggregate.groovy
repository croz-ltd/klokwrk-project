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
package org.klokwrk.cargotracker.booking.domain.model.aggregate

import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.messaging.MetaData
import org.axonframework.modelling.command.AggregateCreationPolicy
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.CreationPolicy
import org.axonframework.spring.stereotype.Aggregate
import org.klokwrk.cargotracker.booking.domain.model.command.BookCargoCommand
import org.klokwrk.cargotracker.booking.domain.model.event.CargoBookedEvent
import org.klokwrk.cargotracker.booking.domain.model.value.CargoId
import org.klokwrk.cargotracker.booking.domain.model.value.Location
import org.klokwrk.cargotracker.booking.domain.model.value.RouteSpecification
import org.klokwrk.cargotracker.lib.boundary.api.exception.CommandException
import org.klokwrk.cargotracker.lib.boundary.api.violation.ViolationInfo
import org.klokwrk.lang.groovy.transform.options.RelaxedPropertyHandler

import static org.axonframework.modelling.command.AggregateLifecycle.apply

@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@MapConstructor(noArg = true)
@Aggregate
@CompileStatic
class CargoAggregate {
  static final String VIOLATION_DESTINATION_LOCATION_CANNOT_ACCEPT_CARGO = "destinationLocationCannotAcceptCargo"

  CargoId cargoId
  Location originLocation
  Location destinationLocation

  @AggregateIdentifier
  String getAggregateIdentifier() {
    // Note: Must use null safe navigation here as cargoId might be null when first command is not successful (and axon requires aggregate identifier for further processing)
    return cargoId?.identifier
  }

  @CommandHandler
  @CreationPolicy(AggregateCreationPolicy.ALWAYS)
  CargoAggregate bookCargo(BookCargoCommand bookCargoCommand, MetaData metaData) {
    // Note: Following validation logic does not require the aggregate state, so it is more appropriate to execute it during the command preparation (in application service or command constructor).
    //       Nevertheless, validation is included here to demonstrate how stateful validation (one that actually requires aggregate state) can be implemented.
    //       I may move this validation to a more appropriate place once we have implemented other examples of stateful business validation.
    RouteSpecification routeSpecification = new RouteSpecification(originLocation: bookCargoCommand.originLocation, destinationLocation: bookCargoCommand.destinationLocation)
    if (!routeSpecification.canDestinationAcceptCargoFromOrigin()) {
      throw new CommandException(ViolationInfo.createForBadRequestWithCustomCodeKey(VIOLATION_DESTINATION_LOCATION_CANNOT_ACCEPT_CARGO))
    }

    apply(cargoBookedEventFromBookCargoCommand(bookCargoCommand), metaData)
    return this
  }

  CargoBookedEvent cargoBookedEventFromBookCargoCommand(BookCargoCommand bookCargoCommand) {
    return new CargoBookedEvent(bookCargoCommand.properties)
  }

  @EventSourcingHandler
  void onCargoBookedEvent(CargoBookedEvent cargoBookedEvent) {
    cargoId = cargoBookedEvent.cargoId
    originLocation = cargoBookedEvent.originLocation
    destinationLocation = cargoBookedEvent.destinationLocation
  }
}
