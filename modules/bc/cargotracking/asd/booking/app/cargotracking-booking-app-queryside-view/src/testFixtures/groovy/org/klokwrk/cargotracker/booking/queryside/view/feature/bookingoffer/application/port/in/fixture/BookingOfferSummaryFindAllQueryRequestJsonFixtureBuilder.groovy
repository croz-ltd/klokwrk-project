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
package org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.fixture

import groovy.json.JsonOutput
import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.fixture.data.PageRequirementJsonFixtureBuilder
import org.klokwrk.cargotracker.booking.queryside.view.feature.bookingoffer.application.port.in.fixture.data.SortRequirementJsonFixtureBuilder
import org.klokwrk.cargotracking.test.support.fixture.base.JsonFixtureBuilder

import static org.klokwrk.cargotracking.test.support.fixture.util.JsonFixtureUtils.jsonFixtureBuilderListToJsonListString
import static org.klokwrk.cargotracking.test.support.fixture.util.JsonFixtureUtils.jsonFixtureBuilderListToJsonList
import static org.klokwrk.cargotracking.test.support.fixture.util.JsonFixtureUtils.stringToJsonString

@Builder(builderStrategy = SimpleStrategy, prefix = "")
@CompileStatic
class BookingOfferSummaryFindAllQueryRequestJsonFixtureBuilder implements JsonFixtureBuilder {
  static BookingOfferSummaryFindAllQueryRequestJsonFixtureBuilder bookingOfferSummaryFindAllQueryRequest_standardCustomer() {
    BookingOfferSummaryFindAllQueryRequestJsonFixtureBuilder jsonFixtureBuilder = new BookingOfferSummaryFindAllQueryRequestJsonFixtureBuilder()
        .userId("standard-customer@cargotracker.com")

    return jsonFixtureBuilder
  }

  String userId
  PageRequirementJsonFixtureBuilder pageRequirement
  List<SortRequirementJsonFixtureBuilder> sortRequirementList

  @Override
  Map<String, ?> buildAsMap() {
    Map<String, ?> mapToReturn = [
        userId: userId
    ]

    pageRequirement ? mapToReturn.pageRequirement = pageRequirement.buildAsMap() : mapToReturn
    sortRequirementList ? mapToReturn.sortRequirementList = jsonFixtureBuilderListToJsonList(sortRequirementList) : mapToReturn

    return mapToReturn
  }

  @SuppressWarnings("DuplicatedCode")
  @Override
  String buildAsJsonString() {
    String jsonStringContent = /"userId": ${ stringToJsonString(userId) }/

    pageRequirement ? jsonStringContent += /, "pageRequirement": ${ pageRequirement.buildAsJsonString() }/ : jsonStringContent
    sortRequirementList ? jsonStringContent += /, "sortRequirementList": ${ jsonFixtureBuilderListToJsonListString(sortRequirementList) }/ : jsonStringContent

    String stringToReturn = /{$jsonStringContent}/
    return JsonOutput.prettyPrint(stringToReturn)
  }
}
