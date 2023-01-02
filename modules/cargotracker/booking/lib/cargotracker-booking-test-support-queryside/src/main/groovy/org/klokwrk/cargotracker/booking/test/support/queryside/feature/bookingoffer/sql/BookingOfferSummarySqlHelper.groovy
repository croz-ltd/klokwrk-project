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
package org.klokwrk.cargotracker.booking.test.support.queryside.feature.bookingoffer.sql

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.transform.CompileStatic

@CompileStatic
class BookingOfferSummarySqlHelper {
  static Integer selectCurrentBookingOfferSummaryRecordsCount(Sql groovySql) {
    GroovyRowResult groovyRowResult = groovySql.firstRow("SELECT count(*) as recordsCount from booking_offer_summary")
    return groovyRowResult.recordsCount as Integer
  }

  static Integer selectCurrentBookingOfferSummaryRecordsCount_forCustomerIdentifier(Sql groovySql, String customerIdentifier) {
    GroovyRowResult groovyRowResult = groovySql.firstRow(
        [customerIdentifier: customerIdentifier],
        "SELECT count(*) as recordsCount from booking_offer_summary where customer_identifier = :customerIdentifier"
    )
    return groovyRowResult.recordsCount as Integer
  }

  static Map<String, ?> selectBookingOfferSummaryRecord(Sql groovySql, UUID bookingOfferIdentifier) {
    // because of join, for each value of contained collection we will have duplicate records of parent columns. So we have to filter the result set.
    List<GroovyRowResult> groovyRowResultList =
        groovySql.rows(
            [bookingOfferIdentifier: bookingOfferIdentifier],
            """
            SELECT bos.*, bos_ct.commodity_type
            FROM booking_offer_summary bos
            LEFT OUTER JOIN booking_offer_summary_commodity_type bos_ct ON bos.booking_offer_identifier = bos_ct.booking_offer_identifier
            WHERE bos.booking_offer_identifier = :bookingOfferIdentifier
            """
        )

    // populate root entity
    Map<String, ?> rootEntityColumns = groovyRowResultList[0]

    // populate collections and remove corresponding element from root
    Set<String> commodityTypeList = groovyRowResultList
        .findAll({ GroovyRowResult groovyRowResult -> groovyRowResult.getProperty("commodity_type") != null })
        .collect({ GroovyRowResult groovyRowResult -> groovyRowResult.getProperty("commodity_type") as String })
        .toSet()
    rootEntityColumns.put("commodity_type_list", commodityTypeList)
    rootEntityColumns.remove("commodity_type")

    return groovyRowResultList[0] as Map<String, ?>
  }
}
