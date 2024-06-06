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
package org.klokwrk.cargotracking.booking.app.commandside.feature.bookingoffer.application.service

import org.klokwrk.cargotracking.booking.app.commandside.feature.bookingoffer.application.port.in.CreateBookingOfferCommandPortIn
import org.klokwrk.cargotracking.booking.app.commandside.feature.bookingoffer.application.port.in.CreateBookingOfferCommandRequest
import org.klokwrk.cargotracking.booking.app.commandside.feature.bookingoffer.application.port.in.CreateBookingOfferCommandResponse
import org.klokwrk.cargotracking.booking.app.commandside.feature.bookingoffer.application.port.in.data.CargoRequestData
import org.klokwrk.cargotracking.booking.app.commandside.feature.bookingoffer.application.port.in.data.RouteSpecificationRequestData
import org.klokwrk.cargotracking.booking.app.commandside.test.base.AbstractCommandSideIntegrationSpecification
import org.klokwrk.cargotracking.booking.lib.boundary.web.metadata.WebMetaDataFixtureBuilder
import org.klokwrk.cargotracking.domain.model.aggregate.BookingOfferCargos
import org.klokwrk.cargotracking.domain.model.value.Cargo
import org.klokwrk.cargotracking.domain.model.value.Commodity
import org.klokwrk.cargotracking.domain.model.value.CommodityType
import org.klokwrk.cargotracking.domain.model.value.ContainerType
import org.klokwrk.cargotracking.lib.boundary.api.application.operation.OperationRequest
import org.klokwrk.cargotracking.lib.boundary.api.application.operation.OperationResponse
import org.klokwrk.lib.xlang.groovy.base.misc.CombUuidShortPrefixUtils
import org.klokwrk.lib.xlang.groovy.base.misc.InstantUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

import java.time.Duration
import java.time.Instant

@SpringBootTest(properties = ['axon.axonserver.servers = ${axonServerInstanceUrl}'])
@ActiveProfiles("testIntegration")
class BookingOfferCommandApplicationServiceIntegrationSpecification extends AbstractCommandSideIntegrationSpecification {
  @Autowired
  CreateBookingOfferCommandPortIn createBookingOfferCommandPortIn

  void "should work for correct request - partial booking offer command - customer"() {
    given:
    String myBookingOfferId = CombUuidShortPrefixUtils.makeCombShortPrefix()
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        userId: "standard-customer@cargotracking.com",
        bookingOfferId: myBookingOfferId,
    )
    Map requestMetadataMap = WebMetaDataFixtureBuilder.webMetaData_booking_default().build()

    when:
    OperationResponse<CreateBookingOfferCommandResponse> createBookingOfferCommandOperationResponse =
        createBookingOfferCommandPortIn.createBookingOfferCommand(new OperationRequest<>(payload: createBookingOfferCommandRequest, metaData: requestMetadataMap))

    CreateBookingOfferCommandResponse createBookingOfferCommandResponsePayload = createBookingOfferCommandOperationResponse.payload
    Map createBookingOfferCommandResponseMetadata = createBookingOfferCommandOperationResponse.metaData

    then:
    createBookingOfferCommandResponseMetadata.isEmpty()
    verifyAll(createBookingOfferCommandResponsePayload) {
      bookingOfferId == myBookingOfferId
      routeSpecification == null
      bookingOfferCargos ==  null
    }
  }

  void "should work for correct request - partial booking offer command - customer and routeSpecification"() {
    given:
    Instant currentInstant = Instant.now()
    Instant departureEarliestTime = currentInstant + Duration.ofHours(1)
    Instant departureLatestTime = currentInstant + Duration.ofHours(2)
    Instant arrivalLatestTime = currentInstant + Duration.ofHours(3)

    Instant expectedDepartureEarliestTime = InstantUtils.roundUpInstantToTheHour(departureEarliestTime)
    Instant expectedDepartureLatestTime = InstantUtils.roundUpInstantToTheHour(departureLatestTime)
    Instant expectedArrivalLatestTime = InstantUtils.roundUpInstantToTheHour(arrivalLatestTime)

    String myBookingOfferId = CombUuidShortPrefixUtils.makeCombShortPrefix()
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        userId: "standard-customer@cargotracking.com",
        bookingOfferId: myBookingOfferId,
        routeSpecification: new RouteSpecificationRequestData(
            originLocation: "NLRTM", destinationLocation: "HRRJK",
            departureEarliestTime: departureEarliestTime, departureLatestTime: departureLatestTime,
            arrivalLatestTime: arrivalLatestTime
        )
    )
    Map requestMetadataMap = WebMetaDataFixtureBuilder.webMetaData_booking_default().build()

    when:
    OperationResponse<CreateBookingOfferCommandResponse> createBookingOfferCommandOperationResponse =
        createBookingOfferCommandPortIn.createBookingOfferCommand(new OperationRequest<>(payload: createBookingOfferCommandRequest, metaData: requestMetadataMap))

    CreateBookingOfferCommandResponse createBookingOfferCommandResponsePayload = createBookingOfferCommandOperationResponse.payload
    Map createBookingOfferCommandResponseMetadata = createBookingOfferCommandOperationResponse.metaData

    then:
    createBookingOfferCommandResponseMetadata.isEmpty()
    verifyAll(createBookingOfferCommandResponsePayload) {
      bookingOfferId == myBookingOfferId

      verifyAll(it.routeSpecification) {
        originLocation.name == "Rotterdam"
        destinationLocation.name == "Rijeka"
        it.departureEarliestTime == expectedDepartureEarliestTime
        it.departureLatestTime == expectedDepartureLatestTime
        it.arrivalLatestTime == expectedArrivalLatestTime
      }

      bookingOfferCargos == null
    }
  }

  void "should work for correct request - complete booking offer command"() {
    given:
    Instant currentInstant = Instant.now()
    Instant departureEarliestTime = currentInstant + Duration.ofHours(1)
    Instant departureLatestTime = currentInstant + Duration.ofHours(2)
    Instant arrivalLatestTime = currentInstant + Duration.ofHours(3)

    Instant expectedDepartureEarliestTime = InstantUtils.roundUpInstantToTheHour(departureEarliestTime)
    Instant expectedDepartureLatestTime = InstantUtils.roundUpInstantToTheHour(departureLatestTime)
    Instant expectedArrivalLatestTime = InstantUtils.roundUpInstantToTheHour(arrivalLatestTime)

    String myBookingOfferId = CombUuidShortPrefixUtils.makeCombShortPrefix()
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        userId: "standard-customer@cargotracking.com",
        bookingOfferId: myBookingOfferId,
        routeSpecification: new RouteSpecificationRequestData(
            originLocation: "NLRTM", destinationLocation: "HRRJK",
            departureEarliestTime: departureEarliestTime, departureLatestTime: departureLatestTime,
            arrivalLatestTime: arrivalLatestTime
        ),
        cargos: [new CargoRequestData(commodityType: CommodityType.DRY.name(), commodityWeight: 1000.kg, containerDimensionType: "DIMENSION_ISO_22")]
    )
    Map requestMetadataMap = WebMetaDataFixtureBuilder.webMetaData_booking_default().build()

    BookingOfferCargos expectedBookingOfferCargos = new BookingOfferCargos()
    expectedBookingOfferCargos.storeCargoCollectionAddition([Cargo.make(ContainerType.TYPE_ISO_22G1, Commodity.make(CommodityType.DRY, 1000), 20615.kg)])

    when:
    OperationResponse<CreateBookingOfferCommandResponse> createBookingOfferCommandOperationResponse =
        createBookingOfferCommandPortIn.createBookingOfferCommand(new OperationRequest<>(payload: createBookingOfferCommandRequest, metaData: requestMetadataMap))

    CreateBookingOfferCommandResponse createBookingOfferCommandResponsePayload = createBookingOfferCommandOperationResponse.payload
    Map createBookingOfferCommandResponseMetadata = createBookingOfferCommandOperationResponse.metaData

    then:
    createBookingOfferCommandResponseMetadata.isEmpty()
    verifyAll(createBookingOfferCommandResponsePayload) {
      bookingOfferId == myBookingOfferId

      verifyAll(it.routeSpecification) {
        originLocation.name == "Rotterdam"
        destinationLocation.name == "Rijeka"
        it.departureEarliestTime == expectedDepartureEarliestTime
        it.departureLatestTime == expectedDepartureLatestTime
        it.arrivalLatestTime == expectedArrivalLatestTime
      }

      verifyAll(it.bookingOfferCargos) {
        size() == 3
        bookingOfferCargoCollection.size() == expectedBookingOfferCargos.bookingOfferCargoCollection.size()
        bookingOfferCargoCollection.containsAll(expectedBookingOfferCargos.bookingOfferCargoCollection)
        totalCommodityWeight == 1000.kg
        totalContainerTeuCount == 1
      }
    }
  }
}
