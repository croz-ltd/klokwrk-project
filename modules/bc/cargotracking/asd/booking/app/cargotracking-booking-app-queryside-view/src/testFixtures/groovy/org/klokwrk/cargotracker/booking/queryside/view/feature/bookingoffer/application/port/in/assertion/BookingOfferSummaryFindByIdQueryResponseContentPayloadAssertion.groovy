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
package org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.assertion

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import org.klokwrk.cargotracking.test.support.assertion.PageItemAssertionable
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.quantity.QuantityRange

import javax.measure.Quantity
import java.time.Instant

/**
 * Assertion class for a responses of BookingOfferSummaryFindById queries.
 */
@CompileStatic
class BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion implements PageItemAssertionable {
  /**
   * Entry point static assertion method for fluent-style top-level API.
   */
  static BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion assertResponseHasPayloadThat(Map responseMap) {
    responseMap.with {
      assert size() == 2
      assert metaData
      assert metaData instanceof Map
      assert payload != null
      assert payload instanceof Map
    }

    return new BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion(responseMap.payload as Map)
  }

  /**
   * Entry point static assertion method for closure-style top-level API.
   */
  static BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion assertResponseHasPayloadThat(
      Map responseMap,
      @DelegatesTo(value = BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion, strategy = Closure.DELEGATE_FIRST)
      @ClosureParams(
          value = SimpleType,
          options = "org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.assertion.BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion"
      ) Closure aClosure)
  {
    BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion payloadAssertion = assertResponseHasPayloadThat(responseMap)
    aClosure.resolveStrategy = Closure.DELEGATE_FIRST
    aClosure.delegate = payloadAssertion
    aClosure.call(payloadAssertion)

    return payloadAssertion
  }

  private final Map payloadMap

  BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion(Map payloadMap) {
    this.payloadMap = payloadMap
  }

  BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion isEmpty() {
    assert payloadMap.size() == 0
    return this
  }

  BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion isSuccessful() {
    payloadMap.with {
      assert size() == 17

      assert bookingOfferId
      assert customerType

      assert originLocationUnLoCode
      assert originLocationName
      assert originLocationCountryName

      assert destinationLocationUnLoCode
      assert destinationLocationName
      assert destinationLocationCountryName

      assert departureEarliestTime
      assert departureLatestTime
      assert arrivalLatestTime

      assert commodityTypes
      (totalCommodityWeight as Map).with {
        assert size() == 2
        assert value
        assert unitSymbol == "kg"
      }

      assert totalContainerTeuCount

      assert firstEventRecordedAt
      assert lastEventRecordedAt
      assert lastEventSequenceNumber != null
    }

    return this
  }

  BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion hasBookingOfferId(String expectedBookingOfferId) {
    assert payloadMap.bookingOfferId == expectedBookingOfferId
    return this
  }

  BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion hasCustomerTypeOfStandard() {
    assert payloadMap.customerType == "STANDARD"
    return this
  }

  BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion hasOriginLocationOfRijeka() {
    payloadMap.with {
      assert originLocationUnLoCode == "HRRJK"
      assert originLocationName == "Rijeka"
      assert originLocationCountryName == "Croatia"
    }

    return this
  }

  BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion hasDestinationLocationName(String expectedDestinationLocationName) {
    assert payloadMap.destinationLocationName == expectedDestinationLocationName
    return this
  }

  BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion hasDestinationLocationCountryName(String expectedDestinationLocationCountryName) {
    assert payloadMap.destinationLocationCountryName == expectedDestinationLocationCountryName
    return this
  }

  BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion hasDestinationLocationOfRotterdam() {
    payloadMap.with {
      assert destinationLocationUnLoCode == "NLRTM"
      assert destinationLocationName == "Rotterdam"
      assert destinationLocationCountryName == "Netherlands"
    }

    return this
  }

  BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion hasDepartureEarliestTime(Instant expectedDepartureEarliestTime) {
    assert Instant.parse(payloadMap.departureEarliestTime as String) == expectedDepartureEarliestTime
    return this
  }

  BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion hasDepartureEarliestTimeGreaterThan(Instant comparableDepartureEarliestTime) {
    assert Instant.parse(payloadMap.departureEarliestTime as String) > comparableDepartureEarliestTime
    return this
  }

  BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion hasDepartureLatestTime(Instant expectedDepartureLatestTime) {
    assert Instant.parse(payloadMap.departureLatestTime as String) == expectedDepartureLatestTime
    return this
  }

  BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion hasDepartureLatestTimeGreaterThan(Instant comparableDepartureLatestTime) {
    assert Instant.parse(payloadMap.departureLatestTime as String) > comparableDepartureLatestTime
    return this
  }

  BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion hasArrivalLatestTime(Instant expectedArrivalLatestTime) {
    assert Instant.parse(payloadMap.arrivalLatestTime as String) == expectedArrivalLatestTime
    return this
  }

  BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion hasArrivalLatestTimeGreaterThan(Instant comparableArrivalLatestTime) {
    assert Instant.parse(payloadMap.arrivalLatestTime as String) > comparableArrivalLatestTime
    return this
  }

  BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion hasCommodityOfDryTypeWithDefaultWeight() {
    payloadMap.with {
      assert commodityTypes == ["DRY"]
      assert totalContainerTeuCount == 1.00G

      (totalCommodityWeight as Map).with {
        assert value == 1000
        assert unitSymbol == "kg"
      }
    }

    return this
  }

  BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion hasTotalCommodityWeight(Quantity expectedQuantity) {
    (payloadMap.totalCommodityWeight as Map).with {
      assert value == expectedQuantity.value
      assert unitSymbol == expectedQuantity.unit.toString()
    }

    return this
  }

  BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion hasTotalCommodityWeightInInclusiveRange(Quantity comparableLowerQuantityBound, Quantity comparableUpperQuantityBound) {
    (payloadMap.totalCommodityWeight as Map).with {
      Quantity myQuantity = Quantities.getQuantity("${ it.value } ${ it.unitSymbol }")
      QuantityRange quantityRange = QuantityRange.of(comparableLowerQuantityBound, comparableUpperQuantityBound)
      assert quantityRange.contains(myQuantity)
    }

    return this
  }

  BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion hasTotalContainerTeuCount(BigDecimal expectedTotalContainerTeuCount) {
    assert payloadMap.totalContainerTeuCount == expectedTotalContainerTeuCount
    return this
  }

  BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion hasLastEventSequenceNumber(Long expectedLastEventSequenceNumber) {
    assert payloadMap.lastEventSequenceNumber == expectedLastEventSequenceNumber
    return this
  }

  BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion hasEventMetadataOfTheFirstEvent() {
    payloadMap.with {
      assert firstEventRecordedAt == lastEventRecordedAt
      assert lastEventSequenceNumber == 0
    }

    return this
  }

  BookingOfferSummaryFindByIdQueryResponseContentPayloadAssertion hasEventMetadataOfTheFirstEventWithCorrectTiming(Instant comparableOperationStartTime) {
    payloadMap.with {
      assert Instant.parse(firstEventRecordedAt as String) > comparableOperationStartTime
      assert firstEventRecordedAt == lastEventRecordedAt
      assert lastEventSequenceNumber == 0
    }

    return this
  }
}
