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
package org.klokwrk.cargotracker.booking.test.component.featurespec

import org.klokwrk.cargotracker.booking.test.component.test.base.AbstractComponentSpecification
import org.klokwrk.lang.groovy.misc.InstantUtils

import java.time.Duration
import java.time.Instant

import static org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.assertion.CreateBookingOfferCommandResponseWebContentPayloadAssertion.assertResponseHasPayloadThat
import static org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.fixture.CreateBookingOfferCommandRequestJsonFixtureBuilder.createBookingOfferCommandRequest_cargoChilled
import static org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.fixture.CreateBookingOfferCommandRequestJsonFixtureBuilder.createBookingOfferCommandRequest_rijekaToRotterdam_cargoChilled
import static org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.fixture.CreateBookingOfferCommandRequestJsonFixtureBuilder.createBookingOfferCommandRequest_rijekaToRotterdam_cargoDry
import static org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.fixture.data.RouteSpecificationRequestDataJsonFixtureBuilder.routeSpecificationRequestData_rotterdamToRijeka
import static org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.fixture.BookingOfferDetailsFindByIdQueryRequestJsonFixtureBuilder.bookingOfferDetailsFindByIdQueryRequest_standardCustomer
import static org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.fixture.BookingOfferSummaryFindAllQueryRequestJsonFixtureBuilder.bookingOfferSummaryFindAllQueryRequest_standardCustomer
import static org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.fixture.BookingOfferSummaryFindByIdQueryRequestJsonFixtureBuilder.bookingOfferSummaryFindByIdQueryRequest_standardCustomer
import static org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.fixture.BookingOfferSummarySearchAllQueryRequestJsonFixtureBuilder.bookingOfferSummarySearchAllQueryRequest_originOfRijeka
import static org.klokwrk.cargotracker.booking.test.component.test.util.BookingOfferFeatureTestHelpers.assertBookingOfferDetailsFindByIdQueryResponseHasPayloadThat
import static org.klokwrk.cargotracker.booking.test.component.test.util.BookingOfferFeatureTestHelpers.assertBookingOfferSummaryFindAllQueryResponseContentHasPayloadThat
import static org.klokwrk.cargotracker.booking.test.component.test.util.BookingOfferFeatureTestHelpers.assertBookingOfferSummaryFindByIdQueryResponseHasPayloadThat
import static org.klokwrk.cargotracker.booking.test.component.test.util.BookingOfferFeatureTestHelpers.assertBookingOfferSummarySearchAllQueryResponseContentHasPayloadThat
import static org.klokwrk.cargotracker.booking.test.component.test.util.BookingOfferFeatureTestHelpers.bookingOfferDetailsFindById_notFound
import static org.klokwrk.cargotracker.booking.test.component.test.util.BookingOfferFeatureTestHelpers.bookingOfferDetailsFindById_succeeded
import static org.klokwrk.cargotracker.booking.test.component.test.util.BookingOfferFeatureTestHelpers.bookingOfferSummaryFindAll_succeeded
import static org.klokwrk.cargotracker.booking.test.component.test.util.BookingOfferFeatureTestHelpers.bookingOfferSummaryFindById_notFound
import static org.klokwrk.cargotracker.booking.test.component.test.util.BookingOfferFeatureTestHelpers.bookingOfferSummaryFindById_succeeded
import static org.klokwrk.cargotracker.booking.test.component.test.util.BookingOfferFeatureTestHelpers.bookingOfferSummarySearchAll_succeeded
import static org.klokwrk.cargotracker.booking.test.component.test.util.BookingOfferFeatureTestHelpers.createBookingOffer_failed
import static org.klokwrk.cargotracker.booking.test.component.test.util.BookingOfferFeatureTestHelpers.createBookingOffer_succeeded
import static org.klokwrk.cargotracker.booking.test.component.test.util.BookingOfferFeatureTestHelpers.makeCommandRequestBodyList_createBookingOffer
import static org.klokwrk.cargotracker.booking.test.component.test.util.BookingOfferFeatureTestHelpers.makeExpectedDepartureAndArrivalInstants
import static org.klokwrk.cargotracker.lib.test.support.assertion.MetaDataAssertion.assertResponseHasMetaDataThat

class BookingOfferFeatureComponentSpecification extends AbstractComponentSpecification {
  @SuppressWarnings("CodeNarc.PropertyName")
  static Integer countOf_createdBookingOffers_forStandardCustomer = 0

  void setupSpec() {
    // Execute a list of commands with predefined data used for findAll and searchAll queries
    List<String> commandRequestBodyList = makeCommandRequestBodyList_createBookingOffer()
    List<String> createdBookingOfferIdList = []
    commandRequestBodyList.each { String commandRequestBody ->
      Map commandResponseMap = createBookingOffer_succeeded(commandRequestBody, commandSideApp)

      if (commandRequestBody.contains("standard-customer@cargotracker.com")) {
        countOf_createdBookingOffers_forStandardCustomer++
      }

      String commandResponseBookingOfferId = commandResponseMap.payload.bookingOfferId.identifier
      createdBookingOfferIdList << commandResponseBookingOfferId
    }

    // Wait for projection of a event corresponding to the last command
    bookingOfferSummaryFindById_succeeded(
        bookingOfferSummaryFindByIdQueryRequest_standardCustomer()
            .bookingOfferId(createdBookingOfferIdList.last())
            .buildAsJsonString(),
        querySideViewApp
    )
  }

  void "command - createBookingOffer - should create booking offer"() {
    when:
    Map commandResponseMap = createBookingOffer_succeeded(commandBodyParam, commandSideApp)

    then:
    assertResponseHasMetaDataThat(commandResponseMap).isSuccessful()

    and:
    verifyAll(commandResponseMap) {
      verifyAll(it.payload as Map) {
        size() == 4

        customer
        routeSpecification
        bookingOfferCargos

        verifyAll(it.bookingOfferId as Map) {
          size() == 1
          identifier
        }
      }
    }

    where:
    commandBodyParam                                                                      | _
    createBookingOfferCommandRequest_rijekaToRotterdam_cargoDry().buildAsJsonString()     | _
    createBookingOfferCommandRequest_rijekaToRotterdam_cargoChilled().buildAsJsonString() | _
  }

  void "command - createBookingOffer - should not create booking offer for invalid command - invalid destination location"() {
    when:
    Map commandResponseMap = createBookingOffer_failed(
        createBookingOfferCommandRequest_cargoChilled()
            .routeSpecification(routeSpecificationRequestData_rotterdamToRijeka().destinationLocation("HRZAG"))
            .buildAsJsonString(),
        acceptLanguageParam,
        commandSideApp
    )

    then:
    assertResponseHasMetaDataThat(commandResponseMap) {
      isViolationOfDomain_badRequest()
      has_general_locale(localeStringParam)
      has_violation_message(violationMessageParam)
    }

    assertResponseHasPayloadThat(commandResponseMap)
        .isEmpty()

    where:
    acceptLanguageParam | localeStringParam | violationMessageParam
    "hr-HR"             | "hr_HR"           | "Teret nije moguće poslati sa navedene početne lokacije na navedenu ciljnu lokaciju."
    "en"                | "en"              | "Cargo cannot be sent from the specified origin location to the destination location."
  }

  void "query - bookingOfferSummaryFindById - should find created booking offer"() {
    given:
    Instant startTime = Instant.now()
    def (Instant expectedDepartureEarliestTime, Instant expectedDepartureLatestTime, Instant expectedArrivalLatestTime) = makeExpectedDepartureAndArrivalInstants(startTime)

    Map commandResponseMap = createBookingOffer_succeeded(
        createBookingOfferCommandRequest_rijekaToRotterdam_cargoDry().buildAsJsonString(),
        commandSideApp
    )

    String bookingOfferId = commandResponseMap.payload.bookingOfferId.identifier

    expect:
    bookingOfferId

    when:
    Map queryResponseMap = bookingOfferSummaryFindById_succeeded(
        bookingOfferSummaryFindByIdQueryRequest_standardCustomer()
            .bookingOfferId(bookingOfferId)
            .buildAsJsonString(),
        querySideViewApp
    )

    then:
    assertResponseHasMetaDataThat(queryResponseMap).isSuccessful()

    assertBookingOfferSummaryFindByIdQueryResponseHasPayloadThat(queryResponseMap) {
      isSuccessful()
      hasCustomerTypeOfStandard()
      hasOriginLocationOfRijeka()
      hasDestinationLocationOfRotterdam()
      hasDepartureEarliestTime(expectedDepartureEarliestTime)
      hasDepartureLatestTime(expectedDepartureLatestTime)
      hasArrivalLatestTime(expectedArrivalLatestTime)
      hasCommodityOfDryTypeWithDefaultWeight()
      hasTotalContainerTeuCount(1.00G)
      hasEventMetadataOfTheFirstEventWithCorrectTiming(startTime)
    }
  }

  void "query - bookingOfferSummaryFindById - should not find non-existing booking offer"() {
    when:
    Map queryResponseMap = bookingOfferSummaryFindById_notFound(
        bookingOfferSummaryFindByIdQueryRequest_standardCustomer()
            .bookingOfferId(UUID.randomUUID().toString())
            .buildAsJsonString(),
        acceptLanguageParam,
        querySideViewApp
    )

    then:
    assertResponseHasMetaDataThat(queryResponseMap) {
      isViolationOfDomain_notFound()
      has_general_locale(localeStringParam)
      has_violation_message(violationMessageParam)
    }

    assertBookingOfferSummaryFindByIdQueryResponseHasPayloadThat(queryResponseMap)
        .isEmpty()

    where:
    acceptLanguageParam | localeStringParam | violationMessageParam
    "hr-HR"             | "hr_HR"           | "Sumarni izvještaj za željenu ponudu za rezervaciju nije pronađen."
    "en"                | "en"              | "Summary report for specified booking offer is not found."
  }

  @SuppressWarnings("UnnecessaryQualifiedReference")
  void "query - bookingOfferSummaryFindAll - should find existing booking offers with default paging and sorting"() {
    when:
    Map queryResponseMap = bookingOfferSummaryFindAll_succeeded(
        bookingOfferSummaryFindAllQueryRequest_standardCustomer().buildAsJsonString(),
        querySideViewApp
    )

    then:
    assertResponseHasMetaDataThat(queryResponseMap).isSuccessful()

    assertBookingOfferSummaryFindAllQueryResponseContentHasPayloadThat(queryResponseMap) {
      isSuccessful()
      hasPageInfoOfFirstPageWithDefaults()
      hasPageInfoThat {
        hasPageElementsCountGreaterThenOrEqual(this.countOf_createdBookingOffers_forStandardCustomer)
      }
      hasPageContentSizeGreaterThanOrEqual(this.countOf_createdBookingOffers_forStandardCustomer)
      hasPageContentWithAllItemsThat {
        hasCustomerTypeOfStandard()
        hasEventMetadataOfTheFirstEvent()
      }
    }
  }

  void "query - bookingOfferSummarySearchAll - should find existing booking offers with default paging and sorting"() {
    when:
    Map queryResponseMap = bookingOfferSummarySearchAll_succeeded(
        bookingOfferSummarySearchAllQueryRequest_originOfRijeka()
            .destinationLocationCountryName("The United States")
            .totalCommodityWeightFromIncluding(15_000.kg)
            .totalCommodityWeightToIncluding(100_000.kg)
            .buildAsJsonString(),
        querySideViewApp
    )

    then:
    assertResponseHasMetaDataThat(queryResponseMap).isSuccessful()

    assertBookingOfferSummarySearchAllQueryResponseContentHasPayloadThat(queryResponseMap) {
      isSuccessful()
      hasPageInfoOfFirstPageWithDefaults()
      hasPageContentSizeGreaterThanOrEqual(4)
      hasPageContentWithAllItemsThat {
        hasCustomerTypeOfStandard()
        hasOriginLocationOfRijeka()
        hasDestinationLocationCountryName("The United States of America")
        hasTotalCommodityWeightInInclusiveRange(15_000.kg, 100_000.kg)
        hasEventMetadataOfTheFirstEvent()
      }
    }
  }

  void "query - bookingOfferDetailsFindById - should find created booking offer"() {
    given:
    Instant currentTime = Instant.now()
    Instant expectedDepartureEarliestTime = InstantUtils.roundUpInstantToTheHour(currentTime + Duration.ofHours(1))
    Instant expectedDepartureLatestTime = InstantUtils.roundUpInstantToTheHour(currentTime + Duration.ofHours(2))
    Instant expectedArrivalLatestTime = InstantUtils.roundUpInstantToTheHour(currentTime + Duration.ofHours(3))

    Map commandResponseMap = createBookingOffer_succeeded(
        createBookingOfferCommandRequest_rijekaToRotterdam_cargoDry(currentTime).buildAsJsonString(),
        commandSideApp
    )

    String bookingOfferId = commandResponseMap.payload.bookingOfferId.identifier

    expect:
    bookingOfferId

    when:
    Map queryResponseMap = bookingOfferDetailsFindById_succeeded(
        bookingOfferDetailsFindByIdQueryRequest_standardCustomer()
            .bookingOfferId(bookingOfferId)
            .buildAsJsonString(),
        querySideViewApp
    )

    then:
    assertResponseHasMetaDataThat(queryResponseMap).isSuccessful()

    assertBookingOfferDetailsFindByIdQueryResponseHasPayloadThat(queryResponseMap) {
      isSuccessful()
      hasBookingOfferId(bookingOfferId)
      hasCustomerTypeOfStandard()
      hasTotalCommodityWeight(1000.kg)
      hasTotalContainerTeuCount(1.0G)
      hasEventMetadataOfTheFirstEventWithCorrectTiming(currentTime)
      hasCargosWithFirstCargoThat {
        isDryDefaultCargo()
        hasMaxAllowedWeightPerContainer(20615.kg)
      }
      hasRouteSpecificationThat {
        hasCreationTimeGreaterThan(currentTime)
        hasDepartureEarliestTime(expectedDepartureEarliestTime)
        hasDepartureLatestTime(expectedDepartureLatestTime)
        hasArrivalLatestTime(expectedArrivalLatestTime)
        hasOriginLocationOfRijeka()
        hasDestinationLocationOfRotterdam()
      }
    }
  }

  void "query - bookingOfferDetailsFindById - should not find details for non-existing booking offer"() {
    when:
    Map queryResponseMap = bookingOfferDetailsFindById_notFound(
        bookingOfferDetailsFindByIdQueryRequest_standardCustomer()
            .bookingOfferId(UUID.randomUUID().toString())
            .buildAsJsonString(),
        acceptLanguageParam,
        querySideViewApp
    )

    then:
    assertResponseHasMetaDataThat(queryResponseMap) {
      isViolationOfDomain_notFound()
      has_general_locale(localeStringParam)
      has_violation_message(violationMessageParam)
    }

    assertBookingOfferDetailsFindByIdQueryResponseHasPayloadThat(queryResponseMap)
        .isEmpty()

    where:
    acceptLanguageParam | localeStringParam | violationMessageParam
    "hr-HR"             | "hr_HR"           | "Detalji za željenu ponudu za rezervaciju nisu pronađeni."
    "en"                | "en"              | "Details for specified booking offer cannot be found."
  }
}
