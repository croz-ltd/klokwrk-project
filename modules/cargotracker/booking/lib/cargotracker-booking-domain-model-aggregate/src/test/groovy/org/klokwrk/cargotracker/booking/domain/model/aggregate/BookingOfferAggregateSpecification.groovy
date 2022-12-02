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

import org.axonframework.test.aggregate.AggregateTestFixture
import org.axonframework.test.aggregate.ResultValidator
import org.axonframework.test.aggregate.TestExecutor
import org.klokwrk.cargotracker.booking.domain.model.command.CreateBookingOfferCommand
import org.klokwrk.cargotracker.booking.domain.model.command.CreateBookingOfferCommandFixtureBuilder
import org.klokwrk.cargotracker.booking.domain.model.event.BookingOfferCreatedEvent
import org.klokwrk.cargotracker.booking.domain.model.event.BookingOfferCreatedEventFixtureBuilder
import org.klokwrk.cargotracker.booking.domain.model.event.data.CargoEventData
import org.klokwrk.cargotracker.booking.domain.model.event.data.CargoEventDataFixtureBuilder
import org.klokwrk.cargotracker.booking.domain.model.event.data.CustomerEventData
import org.klokwrk.cargotracker.booking.domain.model.event.data.RouteSpecificationEventData
import org.klokwrk.cargotracker.booking.domain.model.event.support.QuantityFormatter
import org.klokwrk.cargotracker.booking.domain.model.service.CargoCreatorService
import org.klokwrk.cargotracker.booking.domain.model.service.ConstantBasedMaxAllowedTeuCountPolicy
import org.klokwrk.cargotracker.booking.domain.model.service.DefaultCargoCreatorService
import org.klokwrk.cargotracker.booking.domain.model.service.MaxAllowedTeuCountPolicy
import org.klokwrk.cargotracker.booking.domain.model.service.PercentBasedMaxAllowedWeightPerContainerPolicy
import org.klokwrk.cargotracker.booking.domain.model.value.Cargo
import org.klokwrk.cargotracker.booking.domain.model.value.Commodity
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityType
import org.klokwrk.cargotracker.booking.domain.model.value.ContainerType
import org.klokwrk.cargotracker.lib.boundary.api.domain.exception.CommandException
import spock.lang.Specification
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.unit.Units

class BookingOfferAggregateSpecification extends Specification {
  AggregateTestFixture aggregateTestFixture

  void setup() {
    CargoCreatorService cargoCreatorService = new DefaultCargoCreatorService(new PercentBasedMaxAllowedWeightPerContainerPolicy(95))
    MaxAllowedTeuCountPolicy maxAllowedTeuCountPolicy = new ConstantBasedMaxAllowedTeuCountPolicy(5000.0)

    aggregateTestFixture = new AggregateTestFixture(BookingOfferAggregate)
    aggregateTestFixture.registerInjectableResource(cargoCreatorService)
    aggregateTestFixture.registerInjectableResource(maxAllowedTeuCountPolicy)
  }

  void "should work when origin and destination locations are both container ports at sea"() {
    given:
    CreateBookingOfferCommand createBookingOfferCommand = CreateBookingOfferCommandFixtureBuilder.createBookingOfferCommand_default().build()
    BookingOfferCreatedEvent expectedBookingOfferCreatedEvent = BookingOfferCreatedEventFixtureBuilder
        .bookingOfferCreatedEvent_default()
        .customer(CustomerEventData.fromCustomer(createBookingOfferCommand.customer))
        .bookingOfferId(createBookingOfferCommand.bookingOfferId.identifier)
        .routeSpecification(RouteSpecificationEventData.fromRouteSpecification(createBookingOfferCommand.routeSpecification))
        .cargos([CargoEventDataFixtureBuilder.cargo_dry().maxAllowedWeightPerContainer("20615 kg").build()])
        .build()

    TestExecutor<BookingOfferAggregate> testExecutor = aggregateTestFixture.givenNoPriorActivity()

    when:
    ResultValidator<BookingOfferAggregate> resultValidator = testExecutor.when(createBookingOfferCommand)

    then:
    resultValidator
        .expectSuccessfulHandlerExecution()
        .expectEvents(expectedBookingOfferCreatedEvent)
  }

  void 'should work with acceptable cargo'() {
    given:
    CreateBookingOfferCommand createBookingOfferCommandWithAcceptableCargo = CreateBookingOfferCommandFixtureBuilder
        .createBookingOfferCommand_default()
        .cargoCommodity(Commodity.make(CommodityType.DRY, 10_000))
        .build()

    TestExecutor<BookingOfferAggregate> testExecutor = aggregateTestFixture.givenNoPriorActivity()
    Cargo expectedCargo = Cargo.make(ContainerType.TYPE_ISO_22G1, createBookingOfferCommandWithAcceptableCargo.cargo.commodity, Quantities.getQuantity(20_615, Units.KILOGRAM))

    BookingOfferCreatedEvent expectedBookingOfferCreatedEvent = new BookingOfferCreatedEvent(
        customer: CustomerEventData.fromCustomer(createBookingOfferCommandWithAcceptableCargo.customer),
        bookingOfferId: createBookingOfferCommandWithAcceptableCargo.bookingOfferId.identifier,
        routeSpecification: RouteSpecificationEventData.fromRouteSpecification(createBookingOfferCommandWithAcceptableCargo.routeSpecification),
        cargos: [CargoEventData.fromCargo(expectedCargo)],
        totalCommodityWeight: QuantityFormatter.instance.format(Quantities.getQuantity(10_000, Units.KILOGRAM)),
        totalContainerTeuCount: 1
    )

    when:
    ResultValidator<BookingOfferAggregate> resultValidator = testExecutor.when(createBookingOfferCommandWithAcceptableCargo)

    then:
    resultValidator.expectEvents(expectedBookingOfferCreatedEvent)

    verifyAll(resultValidator.state.get().wrappedAggregate.aggregateRoot as BookingOfferAggregate, {
      bookingOfferId == createBookingOfferCommandWithAcceptableCargo.bookingOfferId
      routeSpecification == createBookingOfferCommandWithAcceptableCargo.routeSpecification
      bookingOfferCargos.totalCommodityWeight == Quantities.getQuantity(10_000, Units.KILOGRAM)
      bookingOfferCargos.totalContainerTeuCount == 1
      bookingOfferCargos.commodityTypeToCargoMap.size() == 1
      bookingOfferCargos.commodityTypeToCargoMap[CommodityType.DRY] == expectedCargo
    })
  }

  void "should fail when cargo cannot be accepted"() {
    given:
    CreateBookingOfferCommand createBookingOfferCommandWithInvalidCargo = CreateBookingOfferCommandFixtureBuilder
        .createBookingOfferCommand_default()
        .cargoCommodity(Commodity.make(CommodityType.DRY, 5001 * 25_000))
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
