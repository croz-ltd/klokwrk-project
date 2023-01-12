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
package org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.service

import org.klokwrk.cargotracker.booking.boundary.web.metadata.WebMetaDataFixtureBuilder
import org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.CargoData
import org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.CreateBookingOfferCommandPortIn
import org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.CreateBookingOfferCommandRequest
import org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.CreateBookingOfferCommandResponse
import org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.RouteSpecificationData
import org.klokwrk.cargotracker.booking.commandside.test.base.AbstractCommandSideIntegrationSpecification
import org.klokwrk.cargotracker.booking.domain.model.aggregate.BookingOfferCargos
import org.klokwrk.cargotracker.booking.domain.model.value.Cargo
import org.klokwrk.cargotracker.booking.domain.model.value.Commodity
import org.klokwrk.cargotracker.booking.domain.model.value.CommodityType
import org.klokwrk.cargotracker.booking.domain.model.value.ContainerType
import org.klokwrk.cargotracker.lib.boundary.api.application.operation.OperationRequest
import org.klokwrk.cargotracker.lib.boundary.api.application.operation.OperationResponse
import org.klokwrk.lang.groovy.misc.CombUuidShortPrefixUtils
import org.klokwrk.lang.groovy.misc.InstantUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

import java.time.Duration
import java.time.Instant

@SpringBootTest(properties = ['axon.axonserver.servers = ${axonServerFirstInstanceUrl}'])
@ActiveProfiles("testIntegration")
class BookingOfferCommandApplicationServiceIntegrationSpecification extends AbstractCommandSideIntegrationSpecification {
  @Autowired
  CreateBookingOfferCommandPortIn createBookingOfferCommandPortIn

  void "should work for correct request"() {
    given:
    Instant currentInstant = Instant.now()
    Instant currentInstantAndOneHour = currentInstant + Duration.ofHours(1)
    Instant currentInstantAndTwoHours = currentInstant + Duration.ofHours(2)
    Instant currentInstantAndThreeHours = currentInstant + Duration.ofHours(3)

    Instant currentInstantRoundedAndOneHour = InstantUtils.roundUpInstantToTheHour(currentInstantAndOneHour)
    Instant currentInstantRoundedAndTwoHours = InstantUtils.roundUpInstantToTheHour(currentInstantAndTwoHours)
    Instant currentInstantRoundedAndThreeHours = InstantUtils.roundUpInstantToTheHour(currentInstantAndThreeHours)

    String myBookingOfferIdentifier = CombUuidShortPrefixUtils.makeCombShortPrefix()
    CreateBookingOfferCommandRequest createBookingOfferCommandRequest = new CreateBookingOfferCommandRequest(
        userIdentifier: "standard-customer@cargotracker.com",
        bookingOfferIdentifier: myBookingOfferIdentifier,
        routeSpecification: new RouteSpecificationData(
            originLocation: "NLRTM", destinationLocation: "HRRJK",
            departureEarliestTime: currentInstantAndOneHour, departureLatestTime: currentInstantAndTwoHours,
            arrivalLatestTime: currentInstantAndThreeHours
        ),
        cargos: [new CargoData(commodityType: CommodityType.DRY.name(), commodityWeight: 1000.kg, containerDimensionType: "DIMENSION_ISO_22")]
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
      bookingOfferId.identifier == myBookingOfferIdentifier

      verifyAll(it.routeSpecification) {
        originLocation.name == "Rotterdam"
        destinationLocation.name == "Rijeka"
        departureEarliestTime == currentInstantRoundedAndOneHour
        departureLatestTime == currentInstantRoundedAndTwoHours
        arrivalLatestTime == currentInstantRoundedAndThreeHours
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
