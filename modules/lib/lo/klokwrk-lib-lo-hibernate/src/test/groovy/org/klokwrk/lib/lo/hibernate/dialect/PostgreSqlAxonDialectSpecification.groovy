/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2024 CROZ d.o.o, the original author or authors.
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
package org.klokwrk.lib.lo.hibernate.dialect

import spock.lang.Specification

import java.sql.Types

class PostgreSqlAxonDialectSpecification extends Specification {
  void "should return column type of postgresql bytea datatype for JDBC's BLOB type"() {
    given:
    PostgreSqlAxonDialect postgreSqlAxonDialect = new PostgreSqlAxonDialect()

    expect:
    postgreSqlAxonDialect.columnType(Types.BLOB) == "bytea"
  }

  void "should cast JDBC's BLOB type into postgresql bytea datatype"() {
    given:
    PostgreSqlAxonDialect postgreSqlAxonDialect = new PostgreSqlAxonDialect()

    expect:
    postgreSqlAxonDialect.castType(Types.BLOB) == "bytea"
  }
}
