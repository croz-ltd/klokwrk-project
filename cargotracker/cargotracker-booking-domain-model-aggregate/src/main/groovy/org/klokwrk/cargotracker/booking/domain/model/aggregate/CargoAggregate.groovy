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
import org.klokwrk.cargotracker.booking.domain.model.value.RouteSpecification
import org.klokwrk.lang.groovy.transform.options.RelaxedPropertyHandler

import static org.axonframework.modelling.command.AggregateLifecycle.apply

@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@MapConstructor(noArg = true)
@Aggregate
@CompileStatic
class CargoAggregate {
  CargoId cargoId
  RouteSpecification routeSpecification

  @AggregateIdentifier
  String getAggregateIdentifier() {
    // Note: Must use null safe navigation here as cargoId might be null when first command is not successful (and axon requires aggregate identifier for further processing)
    return cargoId?.identifier
  }

  @CommandHandler
  @CreationPolicy(AggregateCreationPolicy.ALWAYS)
  CargoAggregate bookCargo(BookCargoCommand bookCargoCommand, MetaData metaData) {
    apply(new CargoBookedEvent(bookCargoCommand.properties), metaData)
    return this
  }

  @EventSourcingHandler
  void onCargoBookedEvent(CargoBookedEvent cargoBookedEvent) {
    cargoId = cargoBookedEvent.cargoId
    routeSpecification = cargoBookedEvent.routeSpecification
  }
}
