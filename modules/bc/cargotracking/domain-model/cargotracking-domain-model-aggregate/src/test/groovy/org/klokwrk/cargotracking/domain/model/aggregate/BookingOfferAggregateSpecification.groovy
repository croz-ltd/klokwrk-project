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

import org.axonframework.test.aggregate.AggregateTestFixture
import org.axonframework.test.aggregate.ResultValidator
import org.axonframework.test.aggregate.TestExecutor
import org.klokwrk.cargotracking.domain.model.command.CreateBookingOfferCommand
import org.klokwrk.cargotracking.domain.model.command.CreateBookingOfferCommandFixtureBuilder
import org.klokwrk.cargotracking.domain.model.command.data.CargoCommandDataFixtureBuilder
import org.klokwrk.cargotracking.domain.model.event.BookingOfferCreatedEvent
import org.klokwrk.cargotracking.domain.model.event.BookingOfferCreatedEventFixtureBuilder
import org.klokwrk.cargotracking.domain.model.event.CargoAddedEvent
import org.klokwrk.cargotracking.domain.model.event.CargoAddedEventFixtureBuilder
import org.klokwrk.cargotracking.domain.model.event.RouteSpecificationAddedEvent
import org.klokwrk.cargotracking.domain.model.event.RouteSpecificationAddedEventFixtureBuilder
import org.klokwrk.cargotracking.domain.model.event.data.CargoEventData
import org.klokwrk.cargotracking.domain.model.event.data.CargoEventDataFixtureBuilder
import org.klokwrk.cargotracking.domain.model.event.data.CustomerEventData
import org.klokwrk.cargotracking.domain.model.event.data.RouteSpecificationEventData
import org.klokwrk.cargotracking.domain.model.service.CargoCreatorService
import org.klokwrk.cargotracking.domain.model.service.ConstantBasedMaxAllowedTeuCountPolicy
import org.klokwrk.cargotracking.domain.model.service.DefaultCargoCreatorService
import org.klokwrk.cargotracking.domain.model.service.MaxAllowedTeuCountPolicy
import org.klokwrk.cargotracking.domain.model.service.MaxAllowedWeightPerContainerPolicy
import org.klokwrk.cargotracking.domain.model.service.PercentBasedMaxAllowedWeightPerContainerPolicy
import org.klokwrk.cargotracking.domain.model.value.Cargo
import org.klokwrk.cargotracking.domain.model.value.Commodity
import org.klokwrk.cargotracking.domain.model.value.CommodityType
import org.klokwrk.cargotracking.domain.model.value.ContainerType
import org.klokwrk.cargotracking.lib.boundary.api.domain.exception.CommandException
import spock.lang.Specification

import javax.measure.Quantity
import javax.measure.quantity.Mass
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class BookingOfferAggregateSpecification extends Specification {
  MaxAllowedWeightPerContainerPolicy maxAllowedWeightPerContainerPolicy
  AggregateTestFixture aggregateTestFixture

  void setup() {
    maxAllowedWeightPerContainerPolicy = new PercentBasedMaxAllowedWeightPerContainerPolicy(95)
    CargoCreatorService cargoCreatorService = new DefaultCargoCreatorService(maxAllowedWeightPerContainerPolicy)
    MaxAllowedTeuCountPolicy maxAllowedTeuCountPolicy = new ConstantBasedMaxAllowedTeuCountPolicy(5000.0)

    aggregateTestFixture = new AggregateTestFixture(BookingOfferAggregate)
    aggregateTestFixture.registerInjectableResource(cargoCreatorService)
    aggregateTestFixture.registerInjectableResource(maxAllowedTeuCountPolicy)
  }

  void "should work for command with customer only"() {
    given:
    Clock aClock = Clock.fixed(Instant.now(), ZoneId.systemDefault())

    CreateBookingOfferCommand createBookingOfferCommand = CreateBookingOfferCommandFixtureBuilder
        .createBookingOfferCommand_default(aClock)
        .routeSpecification(null)
        .cargos(null)
        .build()

    BookingOfferCreatedEvent expectedBookingOfferCreatedEvent = BookingOfferCreatedEventFixtureBuilder
        .bookingOfferCreatedEvent_default(aClock)
        .bookingOfferId(createBookingOfferCommand.bookingOfferId.identifier)
        .customer(CustomerEventData.fromCustomer(createBookingOfferCommand.customer))
        .build()

    TestExecutor<BookingOfferAggregate> testExecutor = aggregateTestFixture.givenNoPriorActivity()

    when:
    ResultValidator<BookingOfferAggregate> resultValidator = testExecutor.when(createBookingOfferCommand)

    then:
    resultValidator
        .expectSuccessfulHandlerExecution()
        .expectEvents(expectedBookingOfferCreatedEvent)
  }

  void "should work for command with customer and routeSpecification"() {
    given:
    Clock aClock = Clock.fixed(Instant.now(), ZoneId.systemDefault())

    CreateBookingOfferCommand createBookingOfferCommand = CreateBookingOfferCommandFixtureBuilder
        .createBookingOfferCommand_default(aClock)
        .cargos(null)
        .build()

    BookingOfferCreatedEvent expectedBookingOfferCreatedEvent = BookingOfferCreatedEventFixtureBuilder
        .bookingOfferCreatedEvent_default(aClock)
        .bookingOfferId(createBookingOfferCommand.bookingOfferId.identifier)
        .customer(CustomerEventData.fromCustomer(createBookingOfferCommand.customer))
        .build()

    RouteSpecificationAddedEvent expectedRouteSpecificationAddedEvent = RouteSpecificationAddedEventFixtureBuilder
        .routeSpecificationAddedEvent_default(aClock)
        .bookingOfferId(createBookingOfferCommand.bookingOfferId.identifier)
        .build()

    TestExecutor<BookingOfferAggregate> testExecutor = aggregateTestFixture.givenNoPriorActivity()

    when:
    ResultValidator<BookingOfferAggregate> resultValidator = testExecutor.when(createBookingOfferCommand)

    then:
    resultValidator
        .expectSuccessfulHandlerExecution()
        .expectEvents(expectedBookingOfferCreatedEvent, expectedRouteSpecificationAddedEvent)
  }

  void "should work for command with customer, routeSpecification and cargos"() {
    given:
    Clock aClock = Clock.fixed(Instant.now(), ZoneId.systemDefault())

    CreateBookingOfferCommand createBookingOfferCommand = CreateBookingOfferCommandFixtureBuilder.createBookingOfferCommand_default(aClock).build()

    BookingOfferCreatedEvent expectedBookingOfferCreatedEvent = BookingOfferCreatedEventFixtureBuilder
        .bookingOfferCreatedEvent_default(aClock)
        .bookingOfferId(createBookingOfferCommand.bookingOfferId.identifier)
        .customer(CustomerEventData.fromCustomer(createBookingOfferCommand.customer))
        .build()

    RouteSpecificationAddedEvent expectedRouteSpecificationAddedEvent = RouteSpecificationAddedEventFixtureBuilder
        .routeSpecificationAddedEvent_default(aClock)
        .bookingOfferId(createBookingOfferCommand.bookingOfferId.identifier)
        .build()

    CargoAddedEvent expectedCargoAddedEvent = makeExpectedCargoAddedEvent(createBookingOfferCommand, aClock, maxAllowedWeightPerContainerPolicy)

    TestExecutor<BookingOfferAggregate> testExecutor = aggregateTestFixture.givenNoPriorActivity()

    when:
    ResultValidator<BookingOfferAggregate> resultValidator = testExecutor.when(createBookingOfferCommand)

    then:
    resultValidator
        .expectSuccessfulHandlerExecution()
        .expectEvents(expectedBookingOfferCreatedEvent, expectedRouteSpecificationAddedEvent, expectedCargoAddedEvent)
  }

  private CargoAddedEvent makeExpectedCargoAddedEvent(CreateBookingOfferCommand createBookingOfferCommand, Clock aClock, MaxAllowedWeightPerContainerPolicy maxAllowedWeightPerContainerPolicy) {
    CargoEventDataFixtureBuilder cargoEventDataFixtureBuilder = CargoEventDataFixtureBuilder.cargo_dry()
    Quantity<Mass> maxAllowedWeightPerContainer = maxAllowedWeightPerContainerPolicy.maxAllowedWeightPerContainer(cargoEventDataFixtureBuilder.containerType)

    CargoAddedEvent expectedCargoAddedEvent = CargoAddedEventFixtureBuilder
        .cargoAddedEvent_default(aClock)
        .bookingOfferId(createBookingOfferCommand.bookingOfferId.identifier)
        .cargo(cargoEventDataFixtureBuilder.maxAllowedWeightPerContainer(maxAllowedWeightPerContainer).build())
        .build()

    return expectedCargoAddedEvent
  }

  void "should work with acceptable cargo"() {
    given:
    CreateBookingOfferCommand createBookingOfferCommandWithAcceptableCargo = CreateBookingOfferCommandFixtureBuilder
        .createBookingOfferCommand_default()
        .cargos([CargoCommandDataFixtureBuilder.createCargoCommandData_default().commodity(Commodity.make(CommodityType.DRY, 10_000)).build()])
        .build()

    TestExecutor<BookingOfferAggregate> testExecutor = aggregateTestFixture.givenNoPriorActivity()

    BookingOfferCreatedEvent expectedBookingOfferCreatedEvent = new BookingOfferCreatedEvent(
        bookingOfferId: createBookingOfferCommandWithAcceptableCargo.bookingOfferId.identifier,
        customer: CustomerEventData.fromCustomer(createBookingOfferCommandWithAcceptableCargo.customer)
    )

    RouteSpecificationAddedEvent expectedRouteSpecificationAddedEvent = new RouteSpecificationAddedEvent(
        bookingOfferId: createBookingOfferCommandWithAcceptableCargo.bookingOfferId.identifier,
        routeSpecification: RouteSpecificationEventData.fromRouteSpecification(createBookingOfferCommandWithAcceptableCargo.routeSpecification)
    )

    Quantity<Mass> maxAllowedWeightPerContainer = maxAllowedWeightPerContainerPolicy.maxAllowedWeightPerContainer(ContainerType.TYPE_ISO_22G1)
    Cargo expectedBookingOfferCargo = Cargo.make(ContainerType.TYPE_ISO_22G1, createBookingOfferCommandWithAcceptableCargo.cargos[0].commodity, maxAllowedWeightPerContainer)
    CargoAddedEvent expectedCargoAddedEvent = new CargoAddedEvent(
        bookingOfferId: createBookingOfferCommandWithAcceptableCargo.bookingOfferId.identifier,
        cargo: CargoEventData.fromCargo(expectedBookingOfferCargo),
        totalCommodityWeight: 10_000.kg,
        totalContainerTeuCount: 1.00
    )

    when:
    ResultValidator<BookingOfferAggregate> resultValidator = testExecutor.when(createBookingOfferCommandWithAcceptableCargo)

    then:
    resultValidator.expectEvents(expectedBookingOfferCreatedEvent, expectedRouteSpecificationAddedEvent, expectedCargoAddedEvent)

    verifyAll(resultValidator.state.get().wrappedAggregate.aggregateRoot as BookingOfferAggregate, {
      bookingOfferId == createBookingOfferCommandWithAcceptableCargo.bookingOfferId
      routeSpecification == createBookingOfferCommandWithAcceptableCargo.routeSpecification

      BookingOfferCargos.checkIfCargoCollectionIsConsolidated(bookingOfferCargos.bookingOfferCargoCollection)
      bookingOfferCargos.totalCommodityWeight == 10_000.kg
      bookingOfferCargos.totalContainerTeuCount == 1
      bookingOfferCargos.findCargoByExample(expectedBookingOfferCargo) == expectedBookingOfferCargo
      bookingOfferCargos.bookingOfferCargoCollection.size() == 1
    })
  }

  void "should fail when cargo cannot be accepted"() {
    given:
    CreateBookingOfferCommand createBookingOfferCommandWithInvalidCargo = CreateBookingOfferCommandFixtureBuilder
        .createBookingOfferCommand_default()
        .cargos([CargoCommandDataFixtureBuilder.createCargoCommandData_default().commodity(Commodity.make(CommodityType.DRY, 5001 * 25_000)).build()])
        .build()

    TestExecutor<BookingOfferAggregate> testExecutor = aggregateTestFixture.givenNoPriorActivity()

    when:
    ResultValidator<BookingOfferAggregate> resultValidator = testExecutor.when(createBookingOfferCommandWithInvalidCargo)

    then:
    resultValidator
        .expectException(CommandException)
        .expectExceptionMessage("Bad Request")

    (resultValidator.actualException as CommandException).violationInfo.violationCode.resolvableMessageKey == "bookingOfferAggregate.bookingOfferCargos.cannotAcceptCargo"
  }
}
