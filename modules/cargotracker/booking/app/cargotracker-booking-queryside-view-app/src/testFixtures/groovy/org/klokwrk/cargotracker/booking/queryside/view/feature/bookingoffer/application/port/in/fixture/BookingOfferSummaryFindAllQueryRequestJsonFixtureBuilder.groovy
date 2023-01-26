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

import static org.klokwrk.lang.groovy.misc.JsonUtils.stringToJsonString

@Builder(builderStrategy = SimpleStrategy, prefix = "")
@CompileStatic
class BookingOfferSummaryFindAllQueryRequestJsonFixtureBuilder {
  static BookingOfferSummaryFindAllQueryRequestJsonFixtureBuilder bookingOfferSummaryFindAllQueryRequest_standardCustomer() {
    BookingOfferSummaryFindAllQueryRequestJsonFixtureBuilder jsonFixtureBuilder = new BookingOfferSummaryFindAllQueryRequestJsonFixtureBuilder()
        .userIdentifier("standard-customer@cargotracker.com")

    return jsonFixtureBuilder
  }

  String userIdentifier
  PageRequirementJsonFixtureBuilder pageRequirement
  List<SortRequirementJsonFixtureBuilder> sortRequirementList

  Map<String, ?> buildAsMap() {
    Map<String, ?> mapToReturn = [
        userIdentifier: userIdentifier
    ]

    if (pageRequirement) {
      mapToReturn.pageRequirement = pageRequirement.buildAsMap()
    }

    if (sortRequirementList != null) {
      List sortRequirementListToUse = []
      sortRequirementList.each {
        sortRequirementListToUse << it.buildAsMap()
      }

      mapToReturn.sortRequirementList = sortRequirementListToUse
    }

    return mapToReturn
  }

  String buildAsJsonString() {
    String jsonStringContent = /"userIdentifier": ${ stringToJsonString(userIdentifier) }/

    if (pageRequirement) {
      jsonStringContent += /, "pageRequirement": ${ pageRequirement.buildAsJsonString() }/
    }

    if (sortRequirementList != null) {
      String sortRequirementListStringContent = ""

      sortRequirementList.eachWithIndex { SortRequirementJsonFixtureBuilder sortRequirementJsonFixtureBuilder, Integer anIndex ->
        sortRequirementListStringContent += "${ sortRequirementJsonFixtureBuilder.buildAsJsonString() }"
        if (anIndex < sortRequirementList.size() - 1) {
          sortRequirementListStringContent += ","
        }
      }

      jsonStringContent += /, "sortRequirementList": [${ sortRequirementListStringContent }]/
    }

    String stringToReturn = """
        {
            $jsonStringContent
        }
        """

    return JsonOutput.prettyPrint(stringToReturn)
  }
}
