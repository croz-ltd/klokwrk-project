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
package org.klokwrk.cargotracking.domain.model.aggregate

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
import org.klokwrk.cargotracking.domain.model.command.CreateBookingOfferCommand
import org.klokwrk.cargotracking.domain.model.command.data.CargoCommandData
import org.klokwrk.cargotracking.domain.model.event.BookingOfferCreatedEvent
import org.klokwrk.cargotracking.domain.model.event.data.CargoEventData
import org.klokwrk.cargotracking.domain.model.event.data.CustomerEventData
import org.klokwrk.cargotracking.domain.model.event.data.RouteSpecificationEventData
import org.klokwrk.cargotracking.domain.model.service.CargoCreatorService
import org.klokwrk.cargotracking.domain.model.service.MaxAllowedTeuCountPolicy
import org.klokwrk.cargotracking.domain.model.value.BookingOfferId
import org.klokwrk.cargotracking.domain.model.value.Cargo
import org.klokwrk.cargotracking.domain.model.value.Customer
import org.klokwrk.cargotracking.domain.model.value.RouteSpecification
import org.klokwrk.cargotracking.lib.boundary.api.domain.exception.CommandException
import org.klokwrk.cargotracking.lib.boundary.api.domain.violation.ViolationInfo
import org.klokwrk.lib.xlang.groovy.base.transform.options.RelaxedPropertyHandler

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
    Collection<Cargo> inputCargoCollection = []
    createBookingOfferCommand.cargos.forEach { CargoCommandData cargoCommandData ->
      Cargo cargo = cargoCreatorService.from(cargoCommandData.containerDimensionType, cargoCommandData.commodity)
      inputCargoCollection << cargo
    }

    // Check if booking offer can accept cargos addition regarding the total container TEU count of a booking offer.
    // The largest ship in the world can carry 24000 TEU of containers. Based on that fact, we are limiting the total container TEU count per a single booking to the max of 5000 TEUs. Of course, the
    // number of 5000 TEUs is entirely arbitrary and is used only as an example.
    //
    // We could enrich behavior with two different policies here. For example, one limiting container TEU count per commodity type and another limiting container TEU count for the whole booking. In a
    // simpler case, both policies can be the same. We can allocate full booking capacity with a single commodity type in that case.
    //
    if (!bookingOfferCargos.canAcceptCargoCollectionAddition(inputCargoCollection, maxAllowedTeuCountPolicy)) {
      throw new CommandException(
          ViolationInfo.makeForBadRequestWithCustomCodeKey(
              "bookingOfferAggregate.bookingOfferCargos.cannotAcceptCargo",
              [maxAllowedTeuCountPolicy.maxAllowedTeuCount.trunc(0).toBigInteger().toString()]
          )
      )
    }

    // Note: cannot store here directly as state change should happen in event sourcing handler.
    //       Alternative is to publish two events (second one applied after the first one updates the state), but we do not want that.
    Tuple2<Quantity<Mass>, BigDecimal> preCalculatedTotals = bookingOfferCargos.preCalculateTotalsForCargoCollectionAddition(inputCargoCollection, maxAllowedTeuCountPolicy)
    Quantity<Mass> bookingTotalCommodityWeight = preCalculatedTotals.v1
    BigDecimal bookingTotalContainerTeuCount = preCalculatedTotals.v2

    Collection<Cargo> consolidatedCargoCollection = BookingOfferCargos.consolidateCargoCollectionsForCargoAddition(bookingOfferCargos.bookingOfferCargoCollection, inputCargoCollection)

    BookingOfferCreatedEvent bookingOfferCreatedEvent = new BookingOfferCreatedEvent(
        customer: CustomerEventData.fromCustomer(createBookingOfferCommand.customer),
        bookingOfferId: createBookingOfferCommand.bookingOfferId.identifier,
        routeSpecification: RouteSpecificationEventData.fromRouteSpecification(createBookingOfferCommand.routeSpecification),
        cargos: CargoEventData.fromCargoCollection(consolidatedCargoCollection),
        totalCommodityWeight: bookingTotalCommodityWeight,
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
    bookingOfferCargos.storeCargoCollectionAddition(cargoCollection)
  }
}
