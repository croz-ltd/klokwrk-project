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
import org.klokwrk.cargotracking.test.support.fixture.base.JsonFixtureBuilder

import static org.klokwrk.cargotracking.test.support.fixture.util.JsonFixtureUtils.stringToJsonString

@Builder(builderStrategy = SimpleStrategy, prefix = "")
@CompileStatic
class BookingOfferDetailsFindByIdQueryRequestJsonFixtureBuilder implements JsonFixtureBuilder {
  static BookingOfferDetailsFindByIdQueryRequestJsonFixtureBuilder bookingOfferDetailsFindByIdQueryRequest_standardCustomer() {
    BookingOfferDetailsFindByIdQueryRequestJsonFixtureBuilder jsonFixtureBuilder = new BookingOfferDetailsFindByIdQueryRequestJsonFixtureBuilder()
        .userId("standard-customer@cargotracking.com")

    return jsonFixtureBuilder
  }

  String userId
  String bookingOfferId

  @Override
  Map<String, ?> buildAsMap() {
    Map<String, ?> mapToReturn = [
        userId: userId,
        bookingOfferId: bookingOfferId,
    ]

    return mapToReturn
  }

  @Override
  String buildAsJsonString() {
    String stringToReturn = """
        {
            "userId": ${ stringToJsonString(userId) },
            "bookingOfferId": ${ stringToJsonString(bookingOfferId) }
        }
        """

    return JsonOutput.prettyPrint(stringToReturn)
  }
}
