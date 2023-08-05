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
package org.klokwrk.cargotracking.booking.app.queryside.view.feature.bookingoffer.application.port.in.fixture

import groovy.json.JsonOutput
import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import org.klokwrk.cargotracking.booking.app.queryside.view.feature.bookingoffer.application.port.in.fixture.data.PageRequirementJsonFixtureBuilder
import org.klokwrk.cargotracking.booking.app.queryside.view.feature.bookingoffer.application.port.in.fixture.data.SortRequirementJsonFixtureBuilder
import org.klokwrk.cargotracking.test.support.fixture.base.JsonFixtureBuilder

import javax.measure.Quantity
import javax.measure.quantity.Mass
import java.time.Instant

import static org.klokwrk.cargotracking.test.support.fixture.util.JsonFixtureUtils.jsonFixtureBuilderListToJsonList
import static org.klokwrk.cargotracking.test.support.fixture.util.JsonFixtureUtils.jsonFixtureBuilderListToJsonListString
import static org.klokwrk.cargotracking.test.support.fixture.util.JsonFixtureUtils.quantityToJsonMap
import static org.klokwrk.cargotracking.test.support.fixture.util.JsonFixtureUtils.quantityToJsonString
import static org.klokwrk.cargotracking.test.support.fixture.util.JsonFixtureUtils.stringToJsonString

@Builder(builderStrategy = SimpleStrategy, prefix = "")
@CompileStatic
class BookingOfferSummarySearchAllQueryRequestJsonFixtureBuilder implements JsonFixtureBuilder {
  static BookingOfferSummarySearchAllQueryRequestJsonFixtureBuilder bookingOfferSummarySearchAllQueryRequest_standardCustomer() {
    BookingOfferSummarySearchAllQueryRequestJsonFixtureBuilder jsonFixtureBuilder = new BookingOfferSummarySearchAllQueryRequestJsonFixtureBuilder()
        .userId("standard-customer@cargotracking.com")

    return jsonFixtureBuilder
  }

  static BookingOfferSummarySearchAllQueryRequestJsonFixtureBuilder bookingOfferSummarySearchAllQueryRequest_originOfRijeka() {
    BookingOfferSummarySearchAllQueryRequestJsonFixtureBuilder jsonFixtureBuilder = new BookingOfferSummarySearchAllQueryRequestJsonFixtureBuilder()
        .userId("standard-customer@cargotracking.com")
        .customerTypeSearchList(["STANDARD", "GOLD"])
        .originLocationName("Rijeka")

    return jsonFixtureBuilder
  }

  String userId
  List<String> customerTypeSearchList
  String originLocationName
  String originLocationCountryName
  String destinationLocationName
  String destinationLocationCountryName
  Set<String> commodityTypes
  Quantity<Mass> totalCommodityWeightFromIncluding
  Quantity<Mass> totalCommodityWeightToIncluding
  BigDecimal totalContainerTeuCountFromIncluding
  BigDecimal totalContainerTeuCountToIncluding
  Instant firstEventRecordedAtFromIncluding
  Instant firstEventRecordedAtToIncluding
  Instant lastEventRecordedAtFromIncluding
  Instant lastEventRecordedAtToIncluding

  PageRequirementJsonFixtureBuilder pageRequirement
  List<SortRequirementJsonFixtureBuilder> sortRequirementList

  @Override
  Map<String, ?> buildAsMap() {
    Map<String, ?> mapToReturn = [
        userId: userId
    ]

    customerTypeSearchList ? mapToReturn.customerTypeSearchList = customerTypeSearchList : mapToReturn
    originLocationName ? mapToReturn.originLocationName = originLocationName : mapToReturn
    originLocationCountryName ? mapToReturn.originLocationCountryName = originLocationCountryName : mapToReturn
    destinationLocationName ? mapToReturn.destinationLocationName = destinationLocationName : mapToReturn
    destinationLocationCountryName ? mapToReturn.destinationLocationCountryName = destinationLocationCountryName : mapToReturn
    commodityTypes ? mapToReturn.commodityTypes = commodityTypes : mapToReturn
    totalCommodityWeightFromIncluding ? mapToReturn.totalCommodityWeightFromIncluding = quantityToJsonMap(totalCommodityWeightFromIncluding) : mapToReturn
    totalCommodityWeightToIncluding ? mapToReturn.totalCommodityWeightToIncluding = quantityToJsonMap(totalCommodityWeightToIncluding) : mapToReturn
    totalContainerTeuCountFromIncluding != null ? mapToReturn.totalContainerTeuCountFromIncluding = totalContainerTeuCountFromIncluding : mapToReturn
    totalContainerTeuCountToIncluding != null ? mapToReturn.totalContainerTeuCountToIncluding = totalContainerTeuCountToIncluding : mapToReturn
    firstEventRecordedAtFromIncluding ? mapToReturn.firstEventRecordedAtFromIncluding = firstEventRecordedAtFromIncluding : mapToReturn
    firstEventRecordedAtToIncluding ? mapToReturn.firstEventRecordedAtToIncluding = firstEventRecordedAtToIncluding : mapToReturn
    lastEventRecordedAtFromIncluding ? mapToReturn.lastEventRecordedAtFromIncluding = lastEventRecordedAtFromIncluding : mapToReturn
    lastEventRecordedAtToIncluding ? mapToReturn.lastEventRecordedAtToIncluding = lastEventRecordedAtToIncluding : mapToReturn

    pageRequirement ? mapToReturn.pageRequirement = pageRequirement.buildAsMap() : mapToReturn
    sortRequirementList ? mapToReturn.sortRequirementList = jsonFixtureBuilderListToJsonList(sortRequirementList) : mapToReturn

    return mapToReturn
  }

  @SuppressWarnings("DuplicatedCode")
  @Override
  String buildAsJsonString() {
    String jsonStringContent = /"userId": ${ stringToJsonString(userId) }/

    customerTypeSearchList ? jsonStringContent += /, "customerTypeSearchList":[${ customerTypeSearchList.collect({ /"$it"/ }).join(",") }]/ : jsonStringContent
    originLocationName ? jsonStringContent += /, "originLocationName": ${ stringToJsonString(originLocationName) }/ : jsonStringContent
    originLocationCountryName ? jsonStringContent += /, "originLocationCountryName": ${ stringToJsonString(originLocationCountryName) }/ : jsonStringContent
    destinationLocationName ? jsonStringContent += /, "destinationLocationName": ${ stringToJsonString(destinationLocationName) }/ : jsonStringContent
    destinationLocationCountryName ? jsonStringContent += /, "destinationLocationCountryName": ${ stringToJsonString(destinationLocationCountryName) }/ : jsonStringContent
    commodityTypes ? jsonStringContent += /, "commodityTypes":[${ commodityTypes.collect({ /"$it"/ }).join(",") }]/ : jsonStringContent
    totalCommodityWeightFromIncluding ? jsonStringContent += /, "totalCommodityWeightFromIncluding": ${ quantityToJsonString(totalCommodityWeightFromIncluding) }/ : jsonStringContent
    totalCommodityWeightToIncluding ? jsonStringContent += /, "totalCommodityWeightToIncluding": ${ quantityToJsonString(totalCommodityWeightToIncluding) }/ : jsonStringContent
    totalContainerTeuCountFromIncluding != null ? jsonStringContent += /, "totalContainerTeuCountFromIncluding": $totalContainerTeuCountFromIncluding/ : jsonStringContent
    totalContainerTeuCountToIncluding != null ? jsonStringContent += /, "totalContainerTeuCountToIncluding": $totalContainerTeuCountToIncluding/ : jsonStringContent
    firstEventRecordedAtFromIncluding ? jsonStringContent += /, "firstEventRecordedAtFromIncluding": $firstEventRecordedAtFromIncluding/ : jsonStringContent
    firstEventRecordedAtToIncluding ? jsonStringContent += /, "firstEventRecordedAtToIncluding": $firstEventRecordedAtToIncluding/ : jsonStringContent
    lastEventRecordedAtFromIncluding ? jsonStringContent += /, "lastEventRecordedAtFromIncluding": $lastEventRecordedAtFromIncluding/ : jsonStringContent
    lastEventRecordedAtToIncluding ? jsonStringContent += /, "lastEventRecordedAtToIncluding": $lastEventRecordedAtToIncluding/ : jsonStringContent

    pageRequirement ? jsonStringContent += /, "pageRequirement": ${ pageRequirement.buildAsJsonString() }/ : jsonStringContent
    sortRequirementList ? jsonStringContent += /, "sortRequirementList": ${ jsonFixtureBuilderListToJsonListString(sortRequirementList) }/ : jsonStringContent

    String stringToReturn = /{$jsonStringContent}/
    return JsonOutput.prettyPrint(stringToReturn)
  }
}
