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
import org.klokwrk.cargotracking.domain.model.event.CargoAddedEvent
import org.klokwrk.cargotracking.domain.model.event.RouteSpecificationAddedEvent
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
import org.klokwrk.lib.xlang.groovy.base.transform.options.RelaxedPropertyHandler

import javax.measure.Quantity
import javax.measure.quantity.Mass

import static org.axonframework.modelling.command.AggregateLifecycle.apply
import static org.klokwrk.cargotracking.domain.model.aggregate.BookingOfferCargos.calculateTotalsForCargoCollectionAddition
import static org.klokwrk.cargotracking.domain.model.aggregate.BookingOfferCargos.canAcceptCargoCollectionAddition
import static org.klokwrk.cargotracking.domain.model.aggregate.BookingOfferCargos.consolidateCargoCollectionsForCargoAddition
import static org.klokwrk.cargotracking.lib.boundary.api.domain.violation.ViolationInfo.makeForBadRequestWithCustomCodeKey

@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@MapConstructor(noArg = true)
@Aggregate
@CompileStatic
class BookingOfferAggregate {
  BookingOfferId bookingOfferId
  Integer lastEventSequenceNumber = -1
  Customer customer
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
      CreateBookingOfferCommand createBookingOfferCommand, MetaData metaData, CargoCreatorService cargoCreatorService, MaxAllowedTeuCountPolicy maxAllowedTeuCountPolicy)
  {
    BookingOfferCreatedEvent bookingOfferCreatedEvent = new BookingOfferCreatedEvent(
        bookingOfferId: createBookingOfferCommand.bookingOfferId.identifier,
        customer: CustomerEventData.fromCustomer(createBookingOfferCommand.customer)
    )

    RouteSpecificationAddedEvent routeSpecificationAddedEvent = null
    if (createBookingOfferCommand.routeSpecification != null) {
      routeSpecificationAddedEvent = new RouteSpecificationAddedEvent(
          bookingOfferId: createBookingOfferCommand.bookingOfferId.identifier,
          routeSpecification: RouteSpecificationEventData.fromRouteSpecification(createBookingOfferCommand.routeSpecification)
      )
    }

    List<CargoAddedEvent> cargoAddedEventList = []
    if (createBookingOfferCommand.cargos) {
      Collection<Cargo> inputConsolidatedCargoCollection = makeInputConsolidatedCargoCollection(createBookingOfferCommand.cargos, cargoCreatorService)
      Collection<Cargo> existingConsolidatedCargoCollection = bookingOfferCargos.bookingOfferCargoCollection // existingConsolidatedCargoCollection is an empty list for the new aggregate

      if (!canAcceptCargoCollectionAddition(existingConsolidatedCargoCollection, inputConsolidatedCargoCollection, maxAllowedTeuCountPolicy)) {
        throw new CommandException(
            makeForBadRequestWithCustomCodeKey("bookingOfferAggregate.bookingOfferCargos.cannotAcceptCargo", [maxAllowedTeuCountPolicy.maxAllowedTeuCount.trunc(0).toBigInteger().toString()])
        )
      }

      // For each consolidated input cargo we want to create independent CargoAddedEvent. The order of handling input cargos is not important but the order of created events is significant.
      // This is because each CargoAddedEvent contains totalCommodityWeight and totalContainerTeuCount at the point of event creation, where totalCommodityWeight and totalContainerTeuCount are
      // cumulative values at the level of the aggregate. Therefore, when creating CargoAddedEvent we have to take into account all previous events for calculating totals. The order of publishing
      // (or applying) events must be the same as the order of their creation.
      // That way, when we have multiple consolidated input cargos during booking offer creation, we will end up with multiple CargoAddedEvent with increasing totalCommodityWeight and
      // totalContainerTeuCount

      Collection<Cargo> intermediateCargoCollection = []
      inputConsolidatedCargoCollection.each((Cargo cargo) -> {
        intermediateCargoCollection << cargo
        Tuple2<Quantity<Mass>, BigDecimal> preCalculatedTotals = calculateTotalsForCargoCollectionAddition(existingConsolidatedCargoCollection, intermediateCargoCollection)
        Quantity<Mass> bookingTotalCommodityWeight = preCalculatedTotals.v1
        BigDecimal bookingTotalContainerTeuCount = preCalculatedTotals.v2

        CargoAddedEvent cargoAddedEvent = new CargoAddedEvent(
            bookingOfferId: createBookingOfferCommand.bookingOfferId.identifier,
            cargo: CargoEventData.fromCargo(cargo),
            totalCommodityWeight: bookingTotalCommodityWeight,
            totalContainerTeuCount: bookingTotalContainerTeuCount
        )

        cargoAddedEventList << cargoAddedEvent
      })
    }

    apply(bookingOfferCreatedEvent, metaData)

    if (routeSpecificationAddedEvent) {
      apply(routeSpecificationAddedEvent, metaData)
    }

    if (!cargoAddedEventList.isEmpty()) {
      cargoAddedEventList.each((CargoAddedEvent cargoAddedEvent) -> apply(cargoAddedEvent, metaData))
    }

    return this
  }

  protected static Collection<Cargo> makeInputConsolidatedCargoCollection(Collection<CargoCommandData> cargoCommandDataCollection, CargoCreatorService cargoCreatorService) {
    Collection<Cargo> inputCargoCollection = []
    cargoCommandDataCollection.each { CargoCommandData cargoCommandData ->
      Cargo cargo = cargoCreatorService.from(cargoCommandData.containerDimensionType, cargoCommandData.commodity)
      inputCargoCollection << cargo
    }

    Collection<Cargo> inputConsolidatedCargoCollection = consolidateCargoCollectionsForCargoAddition([], inputCargoCollection)
    return inputConsolidatedCargoCollection
  }

  @EventSourcingHandler
  void onBookingOfferCreatedEvent(BookingOfferCreatedEvent bookingOfferCreatedEvent) {
    bookingOfferId = BookingOfferId.make(bookingOfferCreatedEvent.bookingOfferId)
    lastEventSequenceNumber++
    customer = bookingOfferCreatedEvent.customer.toCustomer()
  }

  @EventSourcingHandler
  void onRouteSpecificationAddedEvent(RouteSpecificationAddedEvent routeSpecificationAddedEvent) {
    lastEventSequenceNumber++
    routeSpecification = routeSpecificationAddedEvent.routeSpecification.toRouteSpecification()
  }

  @EventSourcingHandler
  void onCargoAddedEvent(CargoAddedEvent cargoAddedEvent) {
    lastEventSequenceNumber++
    bookingOfferCargos.storeCargoCollectionAddition([cargoAddedEvent.cargo.toCargo()])
  }
}
