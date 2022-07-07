/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracker.booking.queryside.test.feature.bookingoffer.sql

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
    List<GroovyRowResult> groovyRowResultList =
        groovySql.rows([bookingOfferIdentifier: bookingOfferIdentifier], "SELECT * from booking_offer_summary where booking_offer_identifier = :bookingOfferIdentifier")

    return groovyRowResultList[0] as Map<String, ?>
  }
}
