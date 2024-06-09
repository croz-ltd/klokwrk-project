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
package org.klokwrk.cargotracking.booking.app.queryside.view.test.util

import groovy.sql.Sql
import groovy.transform.CompileStatic
import org.awaitility.Awaitility
import org.axonframework.eventhandling.EventBus
import org.klokwrk.cargotracking.booking.test.support.queryside.feature.bookingoffer.sql.BookingOfferSummarySqlHelper
import org.klokwrk.cargotracking.domain.model.event.BookingOfferCreatedEvent
import org.klokwrk.cargotracking.domain.model.event.BookingOfferCreatedEventFixtureBuilder
import org.klokwrk.cargotracking.domain.model.event.CargoAddedEvent
import org.klokwrk.cargotracking.domain.model.event.CargoAddedEventFixtureBuilder
import org.klokwrk.cargotracking.domain.model.event.RouteSpecificationAddedEvent
import org.klokwrk.cargotracking.domain.model.event.RouteSpecificationAddedEventFixtureBuilder
import org.klokwrk.cargotracking.domain.model.event.data.CargoEventDataFixtureBuilder
import org.klokwrk.cargotracking.domain.model.event.data.RouteSpecificationEventDataFixtureBuilder
import org.klokwrk.cargotracking.lib.domain.model.event.BaseEvent

import java.time.Duration

import static org.klokwrk.cargotracking.booking.lib.boundary.web.metadata.WebMetaDataFixtureBuilder.webMetaData_booking_default
import static org.klokwrk.cargotracking.booking.test.support.queryside.axon.GenericDomainEventMessageFactory.makeEventMessage

@SuppressWarnings("CodeNarc.AbcMetric")
@CompileStatic
class BookingOfferQueryTestProjectionHelpers {
  static String waitProjectionBookingOfferSummary_forPartialBookingOfferCreation_withCustomer(EventBus eventBus, Sql groovySql) {
    BookingOfferCreatedEvent bookingOfferCreatedEvent = BookingOfferCreatedEventFixtureBuilder.bookingOfferCreatedEvent_default().build()
    eventBus.publish(makeEventMessage(bookingOfferCreatedEvent, webMetaData_booking_default().build(), 0L))

    String bookingOfferId = bookingOfferCreatedEvent.bookingOfferId
    Long expectedLastEventSequenceNumber = 0

    // Wait for projection to complete
    Awaitility.await().atMost(Duration.ofSeconds(10)).until({
      Long lastEventSequenceNumber = BookingOfferSummarySqlHelper.selectFromBookingOfferSummary_lastEventSequenceNumber(groovySql, UUID.fromString(bookingOfferId))
      return lastEventSequenceNumber == expectedLastEventSequenceNumber
    })

    return bookingOfferId
  }

  static String waitProjectionBookingOfferSummary_forPartialBookingOfferCreation_withCustomerAndRouteSpecification(EventBus eventBus, Sql groovySql) {
    BookingOfferCreatedEvent bookingOfferCreatedEvent = BookingOfferCreatedEventFixtureBuilder.bookingOfferCreatedEvent_default().build()
    eventBus.publish(makeEventMessage(bookingOfferCreatedEvent, webMetaData_booking_default().build(), 0L))

    String bookingOfferId = bookingOfferCreatedEvent.bookingOfferId
    Long expectedLastEventSequenceNumber = 1

    RouteSpecificationAddedEvent routeSpecificationAddedEvent = RouteSpecificationAddedEventFixtureBuilder.routeSpecificationAddedEvent_default()
        .bookingOfferId(bookingOfferId)
        .build()
    eventBus.publish(makeEventMessage(routeSpecificationAddedEvent, webMetaData_booking_default().build(), 1L))

    // Wait for projection to complete
    Awaitility.await().atMost(Duration.ofSeconds(10)).until({
      Long lastEventSequenceNumber = BookingOfferSummarySqlHelper.selectFromBookingOfferSummary_lastEventSequenceNumber(groovySql, UUID.fromString(bookingOfferId))
      return lastEventSequenceNumber == expectedLastEventSequenceNumber
    })

    return bookingOfferId
  }

  static String waitProjectionBookingOfferSummary_forCompleteBookingOfferCreation(EventBus eventBus, Sql groovySql, List<BaseEvent> bookingOfferCreationEvents = null) {
    String bookingOfferId = null
    Long expectedLastEventSequenceNumber = null

    if (bookingOfferCreationEvents == null) {
      expectedLastEventSequenceNumber = 2

      BookingOfferCreatedEvent bookingOfferCreatedEvent = BookingOfferCreatedEventFixtureBuilder.bookingOfferCreatedEvent_default().build()
      eventBus.publish(makeEventMessage(bookingOfferCreatedEvent, webMetaData_booking_default().build(), 0L))

      bookingOfferId = bookingOfferCreatedEvent.bookingOfferId

      RouteSpecificationAddedEvent routeSpecificationAddedEvent = RouteSpecificationAddedEventFixtureBuilder.routeSpecificationAddedEvent_default()
          .bookingOfferId(bookingOfferId)
          .build()
      eventBus.publish(makeEventMessage(routeSpecificationAddedEvent, webMetaData_booking_default().build(), 1L))

      CargoAddedEvent cargoAddedEvent = CargoAddedEventFixtureBuilder.cargoAddedEvent_default()
          .bookingOfferId(bookingOfferId)
          .build()
      eventBus.publish(makeEventMessage(cargoAddedEvent, webMetaData_booking_default().build(), 2L))
    }
    else {
      assert bookingOfferCreationEvents.size() >= 3
      assert bookingOfferCreationEvents[0] instanceof BookingOfferCreatedEvent
      assert bookingOfferCreationEvents[1] instanceof RouteSpecificationAddedEvent
      assert bookingOfferCreationEvents[2] instanceof CargoAddedEvent
      bookingOfferCreationEvents.eachWithIndex({ BaseEvent event, int i ->
        if (i > 2) {
          assert event instanceof CargoAddedEvent
        }
      })

      expectedLastEventSequenceNumber = bookingOfferCreationEvents.size() - 1
      bookingOfferId = (bookingOfferCreationEvents[0] as BookingOfferCreatedEvent).bookingOfferId

      bookingOfferCreationEvents.eachWithIndex({ BaseEvent event, int i ->
        eventBus.publish(makeEventMessage(event, webMetaData_booking_default().build(), i))
      })
    }

    // Wait for projection to complete
    Awaitility.await().atMost(Duration.ofSeconds(10)).until({
      Long lastEventSequenceNumber = BookingOfferSummarySqlHelper.selectFromBookingOfferSummary_lastEventSequenceNumber(groovySql, UUID.fromString(bookingOfferId))
      return lastEventSequenceNumber == expectedLastEventSequenceNumber
    })

    return bookingOfferId
  }

  @SuppressWarnings("CodeNarc.AbcMetric")
  static List<List<BaseEvent>> makePastEvents_forSearch_completeBookingOfferCreation(Integer numOfRepetitions = 1) {
    List<List<BaseEvent>> bookingOfferCreationEventsList = []

    numOfRepetitions.times {
      // 1. rijekaToRotterdam
      List<BaseEvent> bookingOfferCreationEvents = []
      BookingOfferCreatedEvent bookingOfferCreatedEvent = BookingOfferCreatedEventFixtureBuilder.bookingOfferCreatedEvent_default().build()

      bookingOfferCreationEvents << bookingOfferCreatedEvent
      bookingOfferCreationEvents << RouteSpecificationAddedEventFixtureBuilder
          .builder(bookingOfferCreatedEvent.bookingOfferId)
          .routeSpecification(RouteSpecificationEventDataFixtureBuilder.routeSpecification_rijekaToRotterdam().build())
          .build()
      bookingOfferCreationEvents << CargoAddedEventFixtureBuilder
          .builder(bookingOfferCreatedEvent.bookingOfferId)
          .cargo(CargoEventDataFixtureBuilder.cargo_dry().commodityWeight(30000.kg).maxAllowedWeightPerContainer(20615.kg).build())
          .build()

      bookingOfferCreationEventsList << bookingOfferCreationEvents

      // 2. rijekaToHamburg
      bookingOfferCreationEvents = []
      bookingOfferCreatedEvent = BookingOfferCreatedEventFixtureBuilder.bookingOfferCreatedEvent_default().build()

      bookingOfferCreationEvents << bookingOfferCreatedEvent
      bookingOfferCreationEvents << RouteSpecificationAddedEventFixtureBuilder
          .builder(bookingOfferCreatedEvent.bookingOfferId)
          .routeSpecification(RouteSpecificationEventDataFixtureBuilder.routeSpecification_rijekaToHamburg().build())
          .build()
      bookingOfferCreationEvents << CargoAddedEventFixtureBuilder
          .builder(bookingOfferCreatedEvent.bookingOfferId)
          .cargo(CargoEventDataFixtureBuilder.cargo_airCooled().commodityWeight(30000.kg).maxAllowedWeightPerContainer(20615.kg).build())
          .build()

      bookingOfferCreationEventsList << bookingOfferCreationEvents

      // 3. rijekaToLosAngeles
      bookingOfferCreationEvents = []
      bookingOfferCreatedEvent = BookingOfferCreatedEventFixtureBuilder.bookingOfferCreatedEvent_default().build()

      bookingOfferCreationEvents << bookingOfferCreatedEvent
      bookingOfferCreationEvents << RouteSpecificationAddedEventFixtureBuilder
          .builder(bookingOfferCreatedEvent.bookingOfferId)
          .routeSpecification(RouteSpecificationEventDataFixtureBuilder.routeSpecification_rijekaToLosAngeles().build())
          .build()
      bookingOfferCreationEvents << CargoAddedEventFixtureBuilder
          .builder(bookingOfferCreatedEvent.bookingOfferId)
          .cargo(CargoEventDataFixtureBuilder.cargo_chilled().commodityWeight(30000.kg).maxAllowedWeightPerContainer(20615.kg).build())
          .build()

      bookingOfferCreationEventsList << bookingOfferCreationEvents

      // 4. rijekaToNewYork
      bookingOfferCreationEvents = []
      bookingOfferCreatedEvent = BookingOfferCreatedEventFixtureBuilder.bookingOfferCreatedEvent_default().build()

      bookingOfferCreationEvents << bookingOfferCreatedEvent
      bookingOfferCreationEvents << RouteSpecificationAddedEventFixtureBuilder
          .builder(bookingOfferCreatedEvent.bookingOfferId)
          .routeSpecification(RouteSpecificationEventDataFixtureBuilder.routeSpecification_rijekaToNewYork().build())
          .build()
      bookingOfferCreationEvents << CargoAddedEventFixtureBuilder
          .builder(bookingOfferCreatedEvent.bookingOfferId)
          .cargo(CargoEventDataFixtureBuilder.cargo_frozen().commodityWeight(30000.kg).maxAllowedWeightPerContainer(20615.kg).build())
          .build()

      bookingOfferCreationEventsList << bookingOfferCreationEvents

      // 5. hamburgToLosAngeles
      bookingOfferCreationEvents = []
      bookingOfferCreatedEvent = BookingOfferCreatedEventFixtureBuilder.bookingOfferCreatedEvent_default().build()

      bookingOfferCreationEvents << bookingOfferCreatedEvent
      bookingOfferCreationEvents << RouteSpecificationAddedEventFixtureBuilder
          .builder(bookingOfferCreatedEvent.bookingOfferId)
          .routeSpecification(RouteSpecificationEventDataFixtureBuilder.routeSpecification_hamburgToLosAngeles().build())
          .build()
      bookingOfferCreationEvents << CargoAddedEventFixtureBuilder
          .builder(bookingOfferCreatedEvent.bookingOfferId)
          .cargo(CargoEventDataFixtureBuilder.cargo_airCooled().commodityWeight(70000.kg).maxAllowedWeightPerContainer(20615.kg).build())
          .build()

      bookingOfferCreationEventsList << bookingOfferCreationEvents

      // 6. rotterdamToNewYork
      bookingOfferCreationEvents = []
      bookingOfferCreatedEvent = BookingOfferCreatedEventFixtureBuilder.bookingOfferCreatedEvent_default().build()

      bookingOfferCreationEvents << bookingOfferCreatedEvent
      bookingOfferCreationEvents << RouteSpecificationAddedEventFixtureBuilder
          .builder(bookingOfferCreatedEvent.bookingOfferId)
          .routeSpecification(RouteSpecificationEventDataFixtureBuilder.routeSpecification_rotterdamToNewYork().build())
          .build()
      bookingOfferCreationEvents << CargoAddedEventFixtureBuilder
          .builder(bookingOfferCreatedEvent.bookingOfferId)
          .cargo(CargoEventDataFixtureBuilder.cargo_dry().commodityWeight(40000.kg).maxAllowedWeightPerContainer(20615.kg).build())
          .build()

      bookingOfferCreationEventsList << bookingOfferCreationEvents

      // 7. rijekaToHamburg
      bookingOfferCreationEvents = []
      bookingOfferCreatedEvent = BookingOfferCreatedEventFixtureBuilder.bookingOfferCreatedEvent_default().build()

      bookingOfferCreationEvents << bookingOfferCreatedEvent
      bookingOfferCreationEvents << RouteSpecificationAddedEventFixtureBuilder
          .builder(bookingOfferCreatedEvent.bookingOfferId)
          .routeSpecification(RouteSpecificationEventDataFixtureBuilder.routeSpecification_rijekaToHamburg().build())
          .build()
      bookingOfferCreationEvents << CargoAddedEventFixtureBuilder
          .builder(bookingOfferCreatedEvent.bookingOfferId)
          .cargo(CargoEventDataFixtureBuilder.cargo_dry().commodityWeight(1000.kg).maxAllowedWeightPerContainer(20615.kg).build())
          .build()

      bookingOfferCreationEventsList << bookingOfferCreationEvents

      // 8. rijekaToLosAngeles
      bookingOfferCreationEvents = []
      bookingOfferCreatedEvent = BookingOfferCreatedEventFixtureBuilder.bookingOfferCreatedEvent_default().build()

      bookingOfferCreationEvents << bookingOfferCreatedEvent
      bookingOfferCreationEvents << RouteSpecificationAddedEventFixtureBuilder
          .builder(bookingOfferCreatedEvent.bookingOfferId)
          .routeSpecification(RouteSpecificationEventDataFixtureBuilder.routeSpecification_rijekaToLosAngeles().build())
          .build()
      bookingOfferCreationEvents << CargoAddedEventFixtureBuilder
          .builder(bookingOfferCreatedEvent.bookingOfferId)
          .cargo(CargoEventDataFixtureBuilder.cargo_dry().commodityWeight(15000.kg).maxAllowedWeightPerContainer(20615.kg).build())
          .build()

      bookingOfferCreationEventsList << bookingOfferCreationEvents

      // 9. rijekaToNewYork
      bookingOfferCreationEvents = []
      bookingOfferCreatedEvent = BookingOfferCreatedEventFixtureBuilder.bookingOfferCreatedEvent_default().build()

      bookingOfferCreationEvents << bookingOfferCreatedEvent
      bookingOfferCreationEvents << RouteSpecificationAddedEventFixtureBuilder
          .builder(bookingOfferCreatedEvent.bookingOfferId)
          .routeSpecification(RouteSpecificationEventDataFixtureBuilder.routeSpecification_rijekaToNewYork().build())
          .build()
      bookingOfferCreationEvents << CargoAddedEventFixtureBuilder
          .builder(bookingOfferCreatedEvent.bookingOfferId)
          .cargo(CargoEventDataFixtureBuilder.cargo_dry().commodityWeight(100000.kg).maxAllowedWeightPerContainer(20615.kg).build())
          .build()

      bookingOfferCreationEventsList << bookingOfferCreationEvents

      // 10. hamburgToRotterdam
      bookingOfferCreationEvents = []
      bookingOfferCreatedEvent = BookingOfferCreatedEventFixtureBuilder.bookingOfferCreatedEvent_default().build()

      bookingOfferCreationEvents << bookingOfferCreatedEvent
      bookingOfferCreationEvents << RouteSpecificationAddedEventFixtureBuilder
          .builder(bookingOfferCreatedEvent.bookingOfferId)
          .routeSpecification(RouteSpecificationEventDataFixtureBuilder.routeSpecification_hamburgToRotterdam().build())
          .build()
      bookingOfferCreationEvents << CargoAddedEventFixtureBuilder
          .builder(bookingOfferCreatedEvent.bookingOfferId)
          .cargo(CargoEventDataFixtureBuilder.cargo_dry().commodityWeight(45000.kg).maxAllowedWeightPerContainer(20615.kg).build())
          .build()

      bookingOfferCreationEventsList << bookingOfferCreationEvents
    }

    return bookingOfferCreationEventsList
  }
}
