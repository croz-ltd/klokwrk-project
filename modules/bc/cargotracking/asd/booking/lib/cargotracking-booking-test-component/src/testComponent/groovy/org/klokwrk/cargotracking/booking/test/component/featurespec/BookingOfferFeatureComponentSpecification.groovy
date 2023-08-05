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
package org.klokwrk.cargotracking.booking.test.component.featurespec

import org.klokwrk.cargotracking.booking.test.component.test.base.AbstractComponentSpecification
import spock.lang.Narrative
import spock.lang.Title

import java.time.Instant

import static org.klokwrk.cargotracking.booking.app.commandside.feature.bookingoffer.application.port.in.assertion.CreateBookingOfferCommandResponseWebContentPayloadAssertion.assertResponseHasPayloadThat
import static org.klokwrk.cargotracking.booking.app.commandside.feature.bookingoffer.application.port.in.fixture.CreateBookingOfferCommandRequestJsonFixtureBuilder.createBookingOfferCommandRequest_cargoChilled
import static org.klokwrk.cargotracking.booking.app.commandside.feature.bookingoffer.application.port.in.fixture.CreateBookingOfferCommandRequestJsonFixtureBuilder.createBookingOfferCommandRequest_rijekaToRotterdam_cargoChilled
import static org.klokwrk.cargotracking.booking.app.commandside.feature.bookingoffer.application.port.in.fixture.CreateBookingOfferCommandRequestJsonFixtureBuilder.createBookingOfferCommandRequest_rijekaToRotterdam_cargoDry
import static org.klokwrk.cargotracking.booking.app.commandside.feature.bookingoffer.application.port.in.fixture.data.RouteSpecificationRequestDataJsonFixtureBuilder.routeSpecificationRequestData_rotterdamToRijeka
import static org.klokwrk.cargotracking.booking.app.queryside.view.feature.bookingoffer.application.port.in.fixture.BookingOfferDetailsFindByIdQueryRequestJsonFixtureBuilder.bookingOfferDetailsFindByIdQueryRequest_standardCustomer
import static org.klokwrk.cargotracking.booking.app.queryside.view.feature.bookingoffer.application.port.in.fixture.BookingOfferSummaryFindAllQueryRequestJsonFixtureBuilder.bookingOfferSummaryFindAllQueryRequest_standardCustomer
import static org.klokwrk.cargotracking.booking.app.queryside.view.feature.bookingoffer.application.port.in.fixture.BookingOfferSummaryFindByIdQueryRequestJsonFixtureBuilder.bookingOfferSummaryFindByIdQueryRequest_standardCustomer
import static org.klokwrk.cargotracking.booking.app.queryside.view.feature.bookingoffer.application.port.in.fixture.BookingOfferSummarySearchAllQueryRequestJsonFixtureBuilder.bookingOfferSummarySearchAllQueryRequest_originOfRijeka
import static org.klokwrk.cargotracking.booking.test.component.test.util.BookingOfferFeatureTestHelpers.assertBookingOfferDetailsFindByIdQueryResponseHasPayloadThat
import static org.klokwrk.cargotracking.booking.test.component.test.util.BookingOfferFeatureTestHelpers.assertBookingOfferSummaryFindAllQueryResponseContentHasPayloadThat
import static org.klokwrk.cargotracking.booking.test.component.test.util.BookingOfferFeatureTestHelpers.assertBookingOfferSummaryFindByIdQueryResponseHasPayloadThat
import static org.klokwrk.cargotracking.booking.test.component.test.util.BookingOfferFeatureTestHelpers.assertBookingOfferSummarySearchAllQueryResponseContentHasPayloadThat
import static org.klokwrk.cargotracking.booking.test.component.test.util.BookingOfferFeatureTestHelpers.bookingOfferDetailsFindById_notFound
import static org.klokwrk.cargotracking.booking.test.component.test.util.BookingOfferFeatureTestHelpers.bookingOfferDetailsFindById_succeeded
import static org.klokwrk.cargotracking.booking.test.component.test.util.BookingOfferFeatureTestHelpers.bookingOfferSummaryFindAll_succeeded
import static org.klokwrk.cargotracking.booking.test.component.test.util.BookingOfferFeatureTestHelpers.bookingOfferSummaryFindById_notFound
import static org.klokwrk.cargotracking.booking.test.component.test.util.BookingOfferFeatureTestHelpers.bookingOfferSummaryFindById_succeeded
import static org.klokwrk.cargotracking.booking.test.component.test.util.BookingOfferFeatureTestHelpers.bookingOfferSummarySearchAll_succeeded
import static org.klokwrk.cargotracking.booking.test.component.test.util.BookingOfferFeatureTestHelpers.createBookingOffer_failed
import static org.klokwrk.cargotracking.booking.test.component.test.util.BookingOfferFeatureTestHelpers.createBookingOffer_succeeded
import static org.klokwrk.cargotracking.booking.test.component.test.util.BookingOfferFeatureTestHelpers.makeCommandRequestBodyList_createBookingOffer
import static org.klokwrk.cargotracking.booking.test.component.test.util.BookingOfferFeatureTestHelpers.makeExpectedDepartureAndArrivalInstants
import static org.klokwrk.cargotracking.test.support.assertion.MetaDataAssertion.assertResponseHasMetaDataThat

@Title("Feature: basic booking offer handling")
@Narrative("Ensures that basic booking offer handling works as expected")
class BookingOfferFeatureComponentSpecification extends AbstractComponentSpecification {
  static Integer countOf_createdBookingOffers_forStandardCustomer = 0 // codenarc-disable PropertyName

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
    when: "booking offer is created and response is returned"
    Map commandResponseMap = createBookingOffer_succeeded(commandBodyParam, commandSideApp)

    then: "response includes expected metadata"
    assertResponseHasMetaDataThat(commandResponseMap).isSuccessful()

    and: "response includes expected payload containing booking offer identifier"
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

    where: "booking offer create command JSON body examples are"
    commandBodyParam                                                                      | _
    createBookingOfferCommandRequest_rijekaToRotterdam_cargoDry().buildAsJsonString()     | _
    createBookingOfferCommandRequest_rijekaToRotterdam_cargoChilled().buildAsJsonString() | _
  }

  void "command - createBookingOffer - should not create booking offer for invalid command - invalid destination location"() {
    when: "booking offer creation fails because of invalid destination"
    Map commandResponseMap = createBookingOffer_failed(
        createBookingOfferCommandRequest_cargoChilled()
            .routeSpecification(routeSpecificationRequestData_rotterdamToRijeka().destinationLocation("HRZAG"))
            .buildAsJsonString(),
        acceptLanguageParam,
        commandSideApp
    )

    then: "response metadata describes violation of domain rules including descriptive localized message"
    assertResponseHasMetaDataThat(commandResponseMap) {
      isViolationOfDomain_badRequest()
      has_general_locale(localeStringParam)
      has_violation_message(violationMessageParam)
    }

    and: "response payload is empty"
    assertResponseHasPayloadThat(commandResponseMap)
        .isEmpty()

    where: "localized messages are"
    acceptLanguageParam | localeStringParam | violationMessageParam
    "hr-HR"             | "hr_HR"           | "Teret nije moguće poslati sa navedene početne lokacije na navedenu ciljnu lokaciju."
    "en"                | "en"              | "Cargo cannot be sent from the specified origin location to the destination location."
  }

  void "query - bookingOfferSummaryFindById - should find created booking offer"() {
    given: "sample booking offer is created"
    Instant startTime = Instant.now()
    def (Instant expectedDepartureEarliestTime, Instant expectedDepartureLatestTime, Instant expectedArrivalLatestTime) = makeExpectedDepartureAndArrivalInstants(startTime)

    Map commandResponseMap = createBookingOffer_succeeded(
        createBookingOfferCommandRequest_rijekaToRotterdam_cargoDry().buildAsJsonString(),
        commandSideApp
    )

    String bookingOfferId = commandResponseMap.payload.bookingOfferId.identifier

    expect: "identifier of created booking offer is returned"
    bookingOfferId

    when: "looking for a summary of created booking offer"
    Map queryResponseMap = bookingOfferSummaryFindById_succeeded(
        bookingOfferSummaryFindByIdQueryRequest_standardCustomer()
            .bookingOfferId(bookingOfferId)
            .buildAsJsonString(),
        querySideViewApp
    )

    then: "response includes expected metadata"
    assertResponseHasMetaDataThat(queryResponseMap).isSuccessful()

    and: "response includes expected payload containing correct TEU count"
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
    when: "looking for a summary of non-existing booking offer by identifier"
    Map queryResponseMap = bookingOfferSummaryFindById_notFound(
        bookingOfferSummaryFindByIdQueryRequest_standardCustomer()
            .bookingOfferId(UUID.randomUUID().toString())
            .buildAsJsonString(),
        acceptLanguageParam,
        querySideViewApp
    )

    then: "response metadata describes 'not found' violation including descriptive localized message"
    assertResponseHasMetaDataThat(queryResponseMap) {
      isViolationOfDomain_notFound()
      has_general_locale(localeStringParam)
      has_violation_message(violationMessageParam)
    }

    and: "response payload is empty"
    assertBookingOfferSummaryFindByIdQueryResponseHasPayloadThat(queryResponseMap)
        .isEmpty()

    where: "localized messages are"
    acceptLanguageParam | localeStringParam | violationMessageParam
    "hr-HR"             | "hr_HR"           | "Sumarni izvještaj za željenu ponudu za rezervaciju nije pronađen."
    "en"                | "en"              | "Summary report for specified booking offer is not found."
  }

  void "query - bookingOfferSummaryFindAll - should find existing booking offers with default paging and sorting"() {
    when: "looking for a summary of all booking offers for a customer"
    Map queryResponseMap = bookingOfferSummaryFindAll_succeeded(
        bookingOfferSummaryFindAllQueryRequest_standardCustomer().buildAsJsonString(),
        querySideViewApp
    )

    then: "response includes expected metadata"
    assertResponseHasMetaDataThat(queryResponseMap).isSuccessful()

    and: "response includes a paged payload with booking offer summaries"
    assertBookingOfferSummaryFindAllQueryResponseContentHasPayloadThat(queryResponseMap) {
      isSuccessful()
      hasPageInfoOfFirstPageWithDefaults()
      hasPageInfoThat {
        hasPageElementsCountGreaterThenOrEqual(countOf_createdBookingOffers_forStandardCustomer)
      }
      hasPageContentSizeGreaterThanOrEqual(countOf_createdBookingOffers_forStandardCustomer)
      hasPageContentWithAllItemsThat {
        hasCustomerTypeOfStandard()
        hasEventMetadataOfTheFirstEvent()
      }
    }
  }

  void "query - bookingOfferSummarySearchAll - should find existing booking offers with default paging and sorting"() {
    when: "searching for a summary of booking offers based on provided parameters"
    Map queryResponseMap = bookingOfferSummarySearchAll_succeeded(
        bookingOfferSummarySearchAllQueryRequest_originOfRijeka()
            .destinationLocationCountryName("The United States")
            .totalCommodityWeightFromIncluding(15_000.kg)
            .totalCommodityWeightToIncluding(100_000.kg)
            .buildAsJsonString(),
        querySideViewApp
    )

    then: "response includes expected metadata"
    assertResponseHasMetaDataThat(queryResponseMap).isSuccessful()

    and: "response includes a paged payload with matching booking offer summaries"
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
    given: "sample booking offer is created"
    Instant startTime = Instant.now()
    def (Instant expectedDepartureEarliestTime, Instant expectedDepartureLatestTime, Instant expectedArrivalLatestTime) = makeExpectedDepartureAndArrivalInstants(startTime)

    Map commandResponseMap = createBookingOffer_succeeded(
        createBookingOfferCommandRequest_rijekaToRotterdam_cargoDry(startTime).buildAsJsonString(),
        commandSideApp
    )

    String bookingOfferId = commandResponseMap.payload.bookingOfferId.identifier

    expect: "identifier of created booking offer is returned"
    bookingOfferId

    when: "looking for a details of created booking offer"
    Map queryResponseMap = bookingOfferDetailsFindById_succeeded(
        bookingOfferDetailsFindByIdQueryRequest_standardCustomer()
            .bookingOfferId(bookingOfferId)
            .buildAsJsonString(),
        querySideViewApp
    )

    then: "response includes expected metadata"
    assertResponseHasMetaDataThat(queryResponseMap).isSuccessful()

    and: "response includes expected payload containing all booking offer details"
    assertBookingOfferDetailsFindByIdQueryResponseHasPayloadThat(queryResponseMap) {
      isSuccessful()
      hasBookingOfferId(bookingOfferId)
      hasCustomerTypeOfStandard()
      hasTotalCommodityWeight(1000.kg)
      hasTotalContainerTeuCount(1.0G)
      hasEventMetadataOfTheFirstEventWithCorrectTiming(startTime)
      hasCargosWithFirstCargoThat {
        isDryDefaultCargo()
        hasMaxAllowedWeightPerContainer(20615.kg)
      }
      hasRouteSpecificationThat {
        hasCreationTimeGreaterThan(startTime)
        hasDepartureEarliestTime(expectedDepartureEarliestTime)
        hasDepartureLatestTime(expectedDepartureLatestTime)
        hasArrivalLatestTime(expectedArrivalLatestTime)
        hasOriginLocationOfRijeka()
        hasDestinationLocationOfRotterdam()
      }
    }
  }

  void "query - bookingOfferDetailsFindById - should not find details for non-existing booking offer"() {
    when: "looking for a details of non-existing booking offer by identifier"
    Map queryResponseMap = bookingOfferDetailsFindById_notFound(
        bookingOfferDetailsFindByIdQueryRequest_standardCustomer()
            .bookingOfferId(UUID.randomUUID().toString())
            .buildAsJsonString(),
        acceptLanguageParam,
        querySideViewApp
    )

    then: "response metadata describes 'not found' violation including descriptive localized message"
    assertResponseHasMetaDataThat(queryResponseMap) {
      isViolationOfDomain_notFound()
      has_general_locale(localeStringParam)
      has_violation_message(violationMessageParam)
    }

    and: "response payload is empty"
    assertBookingOfferDetailsFindByIdQueryResponseHasPayloadThat(queryResponseMap)
        .isEmpty()

    where: "localized messages are"
    acceptLanguageParam | localeStringParam | violationMessageParam
    "hr-HR"             | "hr_HR"           | "Detalji za željenu ponudu za rezervaciju nisu pronađeni."
    "en"                | "en"              | "Details for specified booking offer cannot be found."
  }
}
