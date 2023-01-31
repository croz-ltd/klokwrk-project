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

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.assertion.BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.assertion.BookingOfferSummaryPageableQueryResponseContentPayloadAssertion
import org.klokwrk.cargotracker.booking.test.component.test.base.AbstractComponentSpecification
import org.klokwrk.lang.groovy.misc.InstantUtils

import java.time.Duration
import java.time.Instant

import static org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.assertion.CreateBookingOfferCommandResponseWebContentPayloadAssertion.assertResponseContentHasPayloadThat
import static org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.fixture.CreateBookingOfferCommandRequestJsonFixtureBuilder.createBookingOfferCommandRequest_cargoChilled
import static org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.fixture.CreateBookingOfferCommandRequestJsonFixtureBuilder.createBookingOfferCommandRequest_rijekaToRotterdam_cargoChilled
import static org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.fixture.CreateBookingOfferCommandRequestJsonFixtureBuilder.createBookingOfferCommandRequest_rijekaToRotterdam_cargoDry
import static org.klokwrk.cargotracker.booking.commandside.feature.bookingoffer.application.port.in.fixture.data.RouteSpecificationRequestDataJsonFixtureBuilder.routeSpecificationRequestData_rotterdamToRijeka
import static org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.fixture.BookingOfferSummaryFindAllQueryRequestJsonFixtureBuilder.bookingOfferSummaryFindAllQueryRequest_standardCustomer
import static org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.fixture.BookingOfferSummaryFindByIdQueryRequestJsonFixtureBuilder.bookingOfferSummaryFindByIdQueryRequest_standardCustomer
import static org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.fixture.BookingOfferSummarySearchAllQueryRequestJsonFixtureBuilder.bookingOfferSummarySearchAllQueryRequest_originOfRijeka
import static org.klokwrk.cargotracker.booking.test.component.test.util.BookingOfferFeatureTestHelpers.bookingOfferSummaryFindAll_succeeded
import static org.klokwrk.cargotracker.booking.test.component.test.util.BookingOfferFeatureTestHelpers.bookingOfferSummaryFindById_notFound
import static org.klokwrk.cargotracker.booking.test.component.test.util.BookingOfferFeatureTestHelpers.bookingOfferSummaryFindById_succeeded
import static org.klokwrk.cargotracker.booking.test.component.test.util.BookingOfferFeatureTestHelpers.bookingOfferSummarySearchAll_succeeded
import static org.klokwrk.cargotracker.booking.test.component.test.util.BookingOfferFeatureTestHelpers.createBookingOffer_failed
import static org.klokwrk.cargotracker.booking.test.component.test.util.BookingOfferFeatureTestHelpers.createBookingOffer_succeeded
import static org.klokwrk.cargotracker.booking.test.component.test.util.BookingOfferFeatureTestHelpers.makeCommandRequestBodyList_createBookingOffer
import static org.klokwrk.cargotracker.lib.test.support.assertion.ResponseContentMetaDataAssertion.assertResponseContentHasMetaDataThat

class BookingOfferFeatureComponentSpecification extends AbstractComponentSpecification {
  @SuppressWarnings("CodeNarc.PropertyName")
  static Integer countOf_createdBookingOffers_forStandardCustomer = 0

  static BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion assertBookingOfferSummaryFindByIdQueryResponseHasPayloadThat(Map queryResponseContentMap) {
    return BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion.assertResponseContentHasPayloadThat(queryResponseContentMap)
  }

  static BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion assertBookingOfferSummaryFindByIdQueryResponseHasPayloadThat(
      Map queryResponseContentMap,
      @DelegatesTo(value = BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion, strategy = Closure.DELEGATE_FIRST)
      @ClosureParams(
          value = SimpleType,
          options = "org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.assertion.BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion"
      ) Closure aClosure)
  {
    return BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion.assertResponseContentHasPayloadThat(queryResponseContentMap, aClosure)
  }

  static BookingOfferSummaryPageableQueryResponseContentPayloadAssertion assertBookingOfferSummaryFindAllQueryResponseContentHasPayloadThat(
      Map queryResponseContentMap,
      @DelegatesTo(value = BookingOfferSummaryPageableQueryResponseContentPayloadAssertion, strategy = Closure.DELEGATE_FIRST)
      @ClosureParams(
          value = SimpleType,
          options = "org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.assertion.BookingOfferSummaryPageableQueryResponseContentPayloadAssertion"
      ) Closure aClosure)
  {
    return BookingOfferSummaryPageableQueryResponseContentPayloadAssertion.assertResponseContentHasPageablePayloadThat(queryResponseContentMap, aClosure)
  }

  static BookingOfferSummaryPageableQueryResponseContentPayloadAssertion assertBookingOfferSummarySearchAllQueryResponseContentHasPayloadThat(
      Map queryResponseContentMap,
      @DelegatesTo(value = BookingOfferSummaryPageableQueryResponseContentPayloadAssertion, strategy = Closure.DELEGATE_FIRST)
      @ClosureParams(
          value = SimpleType,
          options = "org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.assertion.BookingOfferSummaryPageableQueryResponseContentPayloadAssertion"
      ) Closure aClosure)
  {
    return BookingOfferSummaryPageableQueryResponseContentPayloadAssertion.assertResponseContentHasPageablePayloadThat(queryResponseContentMap, aClosure)
  }

  void setupSpec() {
    // Execute a list of commands with predefined data used for findAll and searchAll queries
    List<String> commandRequestBodyList = makeCommandRequestBodyList_createBookingOffer()
    List<String> createdBookingOfferIdentifierList = []
    commandRequestBodyList.each { String commandRequestBody ->
      Map commandResponseContentMap = createBookingOffer_succeeded(commandRequestBody, "en", commandSideApp)

      if (commandRequestBody.contains("standard-customer@cargotracker.com")) {
        countOf_createdBookingOffers_forStandardCustomer++
      }

      String commandResponseBookingOfferIdentifier = commandResponseContentMap.payload.bookingOfferId.identifier
      createdBookingOfferIdentifierList << commandResponseBookingOfferIdentifier
    }

    // Wait for projection of a event corresponding to the last command
    bookingOfferSummaryFindById_succeeded(
        bookingOfferSummaryFindByIdQueryRequest_standardCustomer()
            .bookingOfferIdentifier(createdBookingOfferIdentifierList.last())
            .buildAsJsonString(),
        "en",
        querySideViewApp
    )
  }

  void "command - createBookingOffer - should create booking offer"() {
    when:
    Map commandResponseContentMap = createBookingOffer_succeeded(commandBodyParam, acceptLanguageParam, commandSideApp)

    then:
    assertResponseContentHasMetaDataThat(commandResponseContentMap) {
      isSuccessful()
      has_general_locale(localeStringParam)
    }

    verifyAll(commandResponseContentMap) {
      size() == 2
      metaData

      verifyAll(it.payload as Map) {
        size() == 4
        bookingOfferId
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
    acceptLanguageParam | localeStringParam | commandBodyParam
    "hr-HR"             | "hr_HR"           | createBookingOfferCommandRequest_rijekaToRotterdam_cargoDry().buildAsJsonString()
    "hr-HR"             | "hr_HR"           | createBookingOfferCommandRequest_rijekaToRotterdam_cargoChilled().buildAsJsonString()
    "en"                | "en"              | createBookingOfferCommandRequest_rijekaToRotterdam_cargoDry().buildAsJsonString()
    "en"                | "en"              | createBookingOfferCommandRequest_rijekaToRotterdam_cargoChilled().buildAsJsonString()
  }

  void "command - createBookingOffer - should not create booking offer for invalid command - invalid destination location"() {
    when:
    Map commandResponseContentMap = createBookingOffer_failed(
        createBookingOfferCommandRequest_cargoChilled()
            .routeSpecification(routeSpecificationRequestData_rotterdamToRijeka().destinationLocation("HRZAG"))
            .buildAsJsonString(),
        acceptLanguageParam,
        commandSideApp
    )

    then:
    assertResponseContentHasMetaDataThat(commandResponseContentMap) {
      isViolationOfDomain_badRequest()
      has_general_locale(localeStringParam)
      has_violation_message(violationMessageParam)
    }

    assertResponseContentHasPayloadThat(commandResponseContentMap)
        .isEmpty()

    where:
    acceptLanguageParam | localeStringParam | violationMessageParam
    "hr-HR"             | "hr_HR"           | "Teret nije moguće poslati sa navedene početne lokacije na navedenu ciljnu lokaciju."
    "en"                | "en"              | "Cargo cannot be sent from the specified origin location to the destination location."
  }

  void "query - bookingOfferSummaryFindById - should find created booking offer"() {
    given:
    Instant currentTime = Instant.now()
    Instant expectedDepartureEarliestTime = InstantUtils.roundUpInstantToTheHour(currentTime + Duration.ofHours(1))
    Instant expectedDepartureLatestTime = InstantUtils.roundUpInstantToTheHour(currentTime + Duration.ofHours(2))
    Instant expectedArrivalLatestTime = InstantUtils.roundUpInstantToTheHour(currentTime + Duration.ofHours(3))

    Map commandResponseContentMap = createBookingOffer_succeeded(
        createBookingOfferCommandRequest_rijekaToRotterdam_cargoDry(currentTime).buildAsJsonString(),
        "en",
        commandSideApp
    )

    String bookingOfferIdentifier = commandResponseContentMap.payload.bookingOfferId.identifier
    assert bookingOfferIdentifier

    when:
    Map queryResponseContentMap = bookingOfferSummaryFindById_succeeded(
        bookingOfferSummaryFindByIdQueryRequest_standardCustomer()
            .bookingOfferIdentifier(bookingOfferIdentifier)
            .buildAsJsonString(),
        acceptLanguageParam,
        querySideViewApp
    )

    then:
    assertResponseContentHasMetaDataThat(queryResponseContentMap) {
      isSuccessful()
      has_general_locale(localeStringParam)
    }

    assertBookingOfferSummaryFindByIdQueryResponseHasPayloadThat(queryResponseContentMap) {
      isSuccessful()
      hasCustomerTypeOfStandard()
      hasOriginLocationOfRijeka()
      hasDestinationLocationOfRotterdam()
      hasDepartureEarliestTime(expectedDepartureEarliestTime)
      hasDepartureLatestTime(expectedDepartureLatestTime)
      hasArrivalLatestTime(expectedArrivalLatestTime)
      hasCommodityOfDryTypeWithDefaultWeight()
      hasTotalContainerTeuCount(1.00G)
      hasEventMetadataOfTheFirstEventWithCorrectTiming(currentTime)
    }

    where:
    acceptLanguageParam | localeStringParam
    "hr-HR"             | "hr_HR"
    "en"                | "en"
  }

  void "query - bookingOfferSummaryFindById - should not find non-existing booking offer"() {
    when:
    Map queryResponseContentMap = bookingOfferSummaryFindById_notFound(
        bookingOfferSummaryFindByIdQueryRequest_standardCustomer()
            .bookingOfferIdentifier(UUID.randomUUID().toString())
            .buildAsJsonString(),
        acceptLanguageParam,
        querySideViewApp
    )

    then:
    assertResponseContentHasMetaDataThat(queryResponseContentMap) {
      isViolationOfDomain_notFound()
      has_general_locale(localeStringParam)
      has_violation_message(violationMessageParam)
    }

    assertBookingOfferSummaryFindByIdQueryResponseHasPayloadThat(queryResponseContentMap)
        .isEmpty()

    where:
    acceptLanguageParam | localeStringParam | violationMessageParam
    "hr-HR"             | "hr_HR"           | "Sumarni izvještaj za željenu ponudu za rezervaciju nije pronađen."
    "en"                | "en"              | "Summary report for specified booking offer is not found."
  }

  @SuppressWarnings("UnnecessaryQualifiedReference")
  void "query - bookingOfferSummaryFindAll - should find existing booking offers with default paging and sorting"() {
    when:
    Map queryResponseContentMap = bookingOfferSummaryFindAll_succeeded(
        bookingOfferSummaryFindAllQueryRequest_standardCustomer().buildAsJsonString(),
        "en",
        querySideViewApp
    )

    then:
    assertResponseContentHasMetaDataThat(queryResponseContentMap) {
      isSuccessful()
      has_general_locale("en")
    }

    assertBookingOfferSummaryFindAllQueryResponseContentHasPayloadThat(queryResponseContentMap) {
      isSuccessful()
      hasPageInfoOfFirstPageWithDefaults()
      hasPageInfoThat {
        hasPageElementsCountGreaterThenOrEqual(this.countOf_createdBookingOffers_forStandardCustomer)
      }
      hasPageContentSizeGreaterThanOrEqual(this.countOf_createdBookingOffers_forStandardCustomer)
      hasPageContentWithAllElementsThat {
        hasCustomerTypeOfStandard()
        hasEventMetadataOfTheFirstEvent()
      }
    }
  }

  void "query - bookingOfferSummarySearchAll - should find existing booking offers with default paging and sorting"() {
    when:
    Map queryResponseContentMap = bookingOfferSummarySearchAll_succeeded(
        bookingOfferSummarySearchAllQueryRequest_originOfRijeka()
            .destinationLocationCountryName("The United States")
            .totalCommodityWeightFromIncluding(15_000.kg)
            .totalCommodityWeightToIncluding(100_000.kg)
            .buildAsJsonString(),
        "en",
        querySideViewApp
    )

    then:
    assertResponseContentHasMetaDataThat(queryResponseContentMap) {
      isSuccessful()
      has_general_locale("en")
    }

    assertBookingOfferSummarySearchAllQueryResponseContentHasPayloadThat(queryResponseContentMap) {
      isSuccessful()
      hasPageInfoOfFirstPageWithDefaults()
      hasPageContentSizeGreaterThanOrEqual(4)
      hasPageContentWithAllElementsThat {
        hasCustomerTypeOfStandard()
        hasOriginLocationOfRijeka()
        hasDestinationLocationCountryName("The United States of America")
        hasTotalCommodityWeightInInclusiveRange(15_000.kg, 100_000.kg)
        hasEventMetadataOfTheFirstEvent()
      }
    }
  }
}
