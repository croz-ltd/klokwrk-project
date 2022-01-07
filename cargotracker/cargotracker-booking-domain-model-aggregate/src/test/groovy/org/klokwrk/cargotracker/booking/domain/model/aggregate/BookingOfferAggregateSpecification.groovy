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
import org.klokwrk.cargotracker.booking.commandside.test.fixtures.feature.cargobooking.CreateBookingOfferCommandFixtures
import org.klokwrk.cargotracker.booking.commandside.test.fixtures.feature.cargobooking.CargoBookedEventFixtures
import org.klokwrk.cargotracker.booking.domain.model.command.CreateBookingOfferCommand
import org.klokwrk.cargotracker.booking.domain.model.event.BookingOfferCreatedEvent
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
    aggregateTestFixture = new AggregateTestFixture(BookingOfferAggregate)
  }

  void "should work when origin and destination locations are both container ports at sea"() {
    given:
    CreateBookingOfferCommand createBookingOfferCommand = CreateBookingOfferCommandFixtures.commandValidRouteSpecification()
    TestExecutor<BookingOfferAggregate> testExecutor = aggregateTestFixture.givenNoPriorActivity()

    when:
    ResultValidator<BookingOfferAggregate> resultValidator = testExecutor.when(createBookingOfferCommand)

    then:
    resultValidator
        .expectSuccessfulHandlerExecution()
        .expectEvents(CargoBookedEventFixtures.eventValidForCommand(createBookingOfferCommand))
  }

  void "should work with acceptable commodity"() {
    given:
    CreateBookingOfferCommand createBookingOfferCommand = CreateBookingOfferCommandFixtures.commandValidCommodityInfo()
    TestExecutor<BookingOfferAggregate> testExecutor = aggregateTestFixture.givenNoPriorActivity()

    Commodity expectedCommodity = new Commodity(
        containerType: ContainerType.TYPE_ISO_22G1,
        commodityInfo: createBookingOfferCommand.commodityInfo,
        maxAllowedWeightPerContainer: Quantities.getQuantity(23_750, Units.KILOGRAM),
        maxRecommendedWeightPerContainer: Quantities.getQuantity(10_000, Units.KILOGRAM),
        containerCount: 1
    )

    BookingOfferCreatedEvent expectedBookingOfferCreatedEvent = new BookingOfferCreatedEvent(
        bookingOfferId: createBookingOfferCommand.bookingOfferId,
        routeSpecification: createBookingOfferCommand.routeSpecification,
        commodity: expectedCommodity,
        bookingTotalCommodityWeight: Quantities.getQuantity(10_000, Units.KILOGRAM),
        bookingTotalContainerCount: 1
    )

    when:
    ResultValidator<BookingOfferAggregate> resultValidator = testExecutor.when(createBookingOfferCommand)

    then:
    resultValidator.expectEvents(expectedBookingOfferCreatedEvent)

    verifyAll(resultValidator.state.get().wrappedAggregate.aggregateRoot as BookingOfferAggregate, {
      bookingOfferId == createBookingOfferCommand.bookingOfferId
      routeSpecification == createBookingOfferCommand.routeSpecification
      bookingOfferCommodities.totalCommodityWeight == Quantities.getQuantity(10_000, Units.KILOGRAM)
      bookingOfferCommodities.totalContainerCount == 1
      bookingOfferCommodities.commodityTypeToCommodityMap.size() == 1
      bookingOfferCommodities.commodityTypeToCommodityMap[CommodityType.DRY] == expectedCommodity
    })
  }

  void "should fail when commodity cannot be accepted"() {
    given:
    CreateBookingOfferCommand createBookingOfferCommand = CreateBookingOfferCommandFixtures.commandInvalidCommodityInfo()
    TestExecutor<BookingOfferAggregate> testExecutor = aggregateTestFixture.givenNoPriorActivity()

    when:
    ResultValidator<BookingOfferAggregate> resultValidator = testExecutor.when(createBookingOfferCommand)

    then:
    resultValidator
        .expectException(CommandException)
        .expectExceptionMessage("Bad Request")

    (resultValidator.actualException as CommandException).violationInfo.violationCode.codeKey == "bookingOfferAggregate.bookingOfferCommodities.cannotAcceptCommodity"
  }
}
