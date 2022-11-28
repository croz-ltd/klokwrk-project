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
import org.klokwrk.cargotracker.booking.domain.model.event.data.CommodityEventData
import org.klokwrk.cargotracker.booking.domain.model.event.data.CommodityEventDataFixtureBuilder
import org.klokwrk.cargotracker.booking.domain.model.event.data.CustomerEventData
import org.klokwrk.cargotracker.booking.domain.model.event.data.RouteSpecificationEventData
import org.klokwrk.cargotracker.booking.domain.model.event.support.QuantityFormatter
import org.klokwrk.cargotracker.booking.domain.model.service.CommodityCreatorService
import org.klokwrk.cargotracker.booking.domain.model.service.ConstantBasedMaxAllowedTeuCountPolicy
import org.klokwrk.cargotracker.booking.domain.model.service.DefaultCommodityCreatorService
import org.klokwrk.cargotracker.booking.domain.model.service.MaxAllowedTeuCountPolicy
import org.klokwrk.cargotracker.booking.domain.model.service.PercentBasedMaxAllowedWeightPerContainerPolicy
import org.klokwrk.cargotracker.booking.domain.model.value.Commodity
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityInfo
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityType
import org.klokwrk.cargotracker.booking.domain.model.value.ContainerType
import org.klokwrk.cargotracker.lib.boundary.api.domain.exception.CommandException
import spock.lang.Specification
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.unit.Units

class BookingOfferAggregateSpecification extends Specification {
  AggregateTestFixture aggregateTestFixture

  void setup() {
    CommodityCreatorService commodityCreatorService = new DefaultCommodityCreatorService(new PercentBasedMaxAllowedWeightPerContainerPolicy(95))
    MaxAllowedTeuCountPolicy maxAllowedTeuCountPolicy = new ConstantBasedMaxAllowedTeuCountPolicy(5000.0)

    aggregateTestFixture = new AggregateTestFixture(BookingOfferAggregate)
    aggregateTestFixture.registerInjectableResource(commodityCreatorService)
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
        .commodities([CommodityEventDataFixtureBuilder.dry_default().maxAllowedWeightPerContainer("20615 kg").build()])
        .build()

    TestExecutor<BookingOfferAggregate> testExecutor = aggregateTestFixture.givenNoPriorActivity()

    when:
    ResultValidator<BookingOfferAggregate> resultValidator = testExecutor.when(createBookingOfferCommand)

    then:
    resultValidator
        .expectSuccessfulHandlerExecution()
        .expectEvents(expectedBookingOfferCreatedEvent)
  }

  void "should work with acceptable commodity"() {
    given:
    CreateBookingOfferCommand createBookingOfferCommandWithAcceptableCommodity = CreateBookingOfferCommandFixtureBuilder
        .createBookingOfferCommand_default()
        .commodityInfo(CommodityInfo.make(CommodityType.DRY, 10_000))
        .build()

    TestExecutor<BookingOfferAggregate> testExecutor = aggregateTestFixture.givenNoPriorActivity()
    Commodity expectedCommodity = Commodity.make(ContainerType.TYPE_ISO_22G1, createBookingOfferCommandWithAcceptableCommodity.commodityInfo, Quantities.getQuantity(20_615, Units.KILOGRAM))

    BookingOfferCreatedEvent expectedBookingOfferCreatedEvent = new BookingOfferCreatedEvent(
        customer: CustomerEventData.fromCustomer(createBookingOfferCommandWithAcceptableCommodity.customer),
        bookingOfferId: createBookingOfferCommandWithAcceptableCommodity.bookingOfferId.identifier,
        routeSpecification: RouteSpecificationEventData.fromRouteSpecification(createBookingOfferCommandWithAcceptableCommodity.routeSpecification),
        commodities: [CommodityEventData.fromCommodity(expectedCommodity)],
        commodityTotalWeight: QuantityFormatter.instance.format(Quantities.getQuantity(10_000, Units.KILOGRAM)),
        commodityTotalContainerTeuCount: 1
    )

    when:
    ResultValidator<BookingOfferAggregate> resultValidator = testExecutor.when(createBookingOfferCommandWithAcceptableCommodity)

    then:
    resultValidator.expectEvents(expectedBookingOfferCreatedEvent)

    verifyAll(resultValidator.state.get().wrappedAggregate.aggregateRoot as BookingOfferAggregate, {
      bookingOfferId == createBookingOfferCommandWithAcceptableCommodity.bookingOfferId
      routeSpecification == createBookingOfferCommandWithAcceptableCommodity.routeSpecification
      bookingOfferCommodities.totalCommodityWeight == Quantities.getQuantity(10_000, Units.KILOGRAM)
      bookingOfferCommodities.totalContainerTeuCount == 1
      bookingOfferCommodities.commodityTypeToCommodityMap.size() == 1
      bookingOfferCommodities.commodityTypeToCommodityMap[CommodityType.DRY] == expectedCommodity
    })
  }

  void "should fail when commodity cannot be accepted"() {
    given:
    CreateBookingOfferCommand createBookingOfferCommandWithInvalidCommodityInfo = CreateBookingOfferCommandFixtureBuilder
        .createBookingOfferCommand_default()
        .commodityInfo(CommodityInfo.make(CommodityType.DRY, 5001 * 25_000))
        .build()

    TestExecutor<BookingOfferAggregate> testExecutor = aggregateTestFixture.givenNoPriorActivity()

    when:
    ResultValidator<BookingOfferAggregate> resultValidator = testExecutor.when(createBookingOfferCommandWithInvalidCommodityInfo)

    then:
    resultValidator
        .expectException(CommandException)
        .expectExceptionMessage("Bad Request")

    (resultValidator.actualException as CommandException).violationInfo.violationCode.resolvableMessageKey == "bookingOfferAggregate.bookingOfferCommodities.cannotAcceptCommodity"
  }
}
