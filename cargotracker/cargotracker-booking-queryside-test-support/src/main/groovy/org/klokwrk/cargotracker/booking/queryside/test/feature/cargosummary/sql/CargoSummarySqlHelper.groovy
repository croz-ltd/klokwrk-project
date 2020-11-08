/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.cargotracker.booking.queryside.test.feature.cargosummary.sql

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.transform.CompileStatic

@CompileStatic
class CargoSummarySqlHelper {
  static Long selectCurrentCargoSummaryRecordsCount(Sql groovySql) {
    GroovyRowResult groovyRowResult = groovySql.firstRow("SELECT count(*) as recordsCount from cargo_summary")
    return groovyRowResult.recordsCount as Long
  }

  static Map<String, ?> selectCargoSummaryRecord(Sql groovySql, String aggregateIdentifier) {
    List<GroovyRowResult> groovyRowResultList = groovySql.rows([aggregateIdentifier: aggregateIdentifier], "SELECT * from cargo_summary where aggregate_identifier = :aggregateIdentifier")
    return groovyRowResultList[0] as Map<String, ?>
  }
}
