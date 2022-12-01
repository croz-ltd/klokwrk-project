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
import org.klokwrk.cargotracker.booking.domain.model.command.CreateBookingOfferCommand
import org.klokwrk.cargotracker.booking.domain.model.event.BookingOfferCreatedEvent
import org.klokwrk.cargotracker.booking.domain.model.event.data.CargoEventData
import org.klokwrk.cargotracker.booking.domain.model.event.data.CustomerEventData
import org.klokwrk.cargotracker.booking.domain.model.event.data.RouteSpecificationEventData
import org.klokwrk.cargotracker.booking.domain.model.event.support.QuantityFormatter
import org.klokwrk.cargotracker.booking.domain.model.service.CargoCreatorService
import org.klokwrk.cargotracker.booking.domain.model.service.MaxAllowedTeuCountPolicy
import org.klokwrk.cargotracker.booking.domain.model.value.BookingOfferId
import org.klokwrk.cargotracker.booking.domain.model.value.Cargo
import org.klokwrk.cargotracker.booking.domain.model.value.Customer
import org.klokwrk.cargotracker.booking.domain.model.value.RouteSpecification
import org.klokwrk.cargotracker.lib.boundary.api.domain.exception.CommandException
import org.klokwrk.cargotracker.lib.boundary.api.domain.violation.ViolationInfo
import org.klokwrk.lang.groovy.transform.options.RelaxedPropertyHandler

import javax.measure.Quantity
import javax.measure.quantity.Mass

import static org.axonframework.modelling.command.AggregateLifecycle.apply

@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@MapConstructor(noArg = true)
@Aggregate
@CompileStatic
class BookingOfferAggregate {
  Customer customer
  BookingOfferId bookingOfferId
  RouteSpecification routeSpecification
  BookingOfferCargos bookingOfferCargos = new BookingOfferCargos()

  @AggregateIdentifier
  String getAggregateIdentifier() {
    // Note: Must use null safe navigation here as cargoId might be null when first command is not successful (and axon requires aggregate identifier for further processing)
    return bookingOfferId?.identifier
  }

  @SuppressWarnings("CodeNarc.FactoryMethodName")
  @CommandHandler
  @CreationPolicy(AggregateCreationPolicy.ALWAYS)
  BookingOfferAggregate createBookingOffer(
      CreateBookingOfferCommand createBookingOfferCommand, MetaData metaData,
      CargoCreatorService cargoCreatorService, MaxAllowedTeuCountPolicy maxAllowedTeuCountPolicy)
  {
    Cargo cargo = cargoCreatorService.from(createBookingOfferCommand.containerDimensionType, createBookingOfferCommand.commodityInfo)

    // Check for container TEU count per commodity type.
    // The largest ship in the world can carry 24000 TEU of containers. We should limit container TEU count to the max of 5000 per a single booking, for example.
    // We can have two different policies here. One for limiting container TEU count per commodity type, and another one for limiting container TEU count for the whole booking.
    // In a simpler case, both policies can be the same. In that case with a single commodity type we can allocate complete booking capacity.
    if (!bookingOfferCargos.canAcceptCargo(cargo, maxAllowedTeuCountPolicy)) {
      throw new CommandException(
          ViolationInfo.makeForBadRequestWithCustomCodeKey(
              "bookingOfferAggregate.bookingOfferCargos.cannotAcceptCargo",
              [maxAllowedTeuCountPolicy.maxAllowedTeuCount.trunc(0).toBigInteger().toString()]
          )
      )
    }

    // Note: cannot store here directly as state change should happen in event sourcing handler.
    //       Alternative is to publish two events (second one applied after the first one updates the state), but we do not want that.
    Tuple2<Quantity<Mass>, BigDecimal> preCalculatedTotals = bookingOfferCargos.preCalculateTotals(cargo, maxAllowedTeuCountPolicy)
    Quantity<Mass> bookingTotalCommodityWeight = preCalculatedTotals.v1
    BigDecimal bookingTotalContainerTeuCount = preCalculatedTotals.v2

    BookingOfferCreatedEvent bookingOfferCreatedEvent = new BookingOfferCreatedEvent(
        customer: CustomerEventData.fromCustomer(createBookingOfferCommand.customer),
        bookingOfferId: createBookingOfferCommand.bookingOfferId.identifier,
        routeSpecification: RouteSpecificationEventData.fromRouteSpecification(createBookingOfferCommand.routeSpecification),
        cargos: CargoEventData.fromCargoCollection([cargo]),
        totalCommodityWeight: QuantityFormatter.instance.format(bookingTotalCommodityWeight),
        totalContainerTeuCount: bookingTotalContainerTeuCount
    )

    apply(bookingOfferCreatedEvent, metaData)
    return this
  }

  @EventSourcingHandler
  void onBookingOfferCreatedEvent(BookingOfferCreatedEvent bookingOfferCreatedEvent) {
    customer = bookingOfferCreatedEvent.customer.toCustomer()
    bookingOfferId = BookingOfferId.make(bookingOfferCreatedEvent.bookingOfferId)
    routeSpecification = bookingOfferCreatedEvent.routeSpecification.toRouteSpecification()

    Collection<Cargo> cargoCollection = bookingOfferCreatedEvent.cargos.collect({ CargoEventData cargoEventData -> cargoEventData.toCargo() })
    bookingOfferCargos.storeCargo(cargoCollection.first())
  }
}
