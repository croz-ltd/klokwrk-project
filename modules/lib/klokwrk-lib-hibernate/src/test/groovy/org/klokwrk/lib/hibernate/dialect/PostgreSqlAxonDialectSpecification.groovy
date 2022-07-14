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
package org.klokwrk.lib.hibernate.dialect

import org.hibernate.type.descriptor.sql.BinaryTypeDescriptor
import org.hibernate.type.descriptor.sql.BlobTypeDescriptor
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor
import org.hibernate.type.descriptor.sql.VarcharTypeDescriptor
import spock.lang.Specification

import java.sql.Types

class PostgreSqlAxonDialectSpecification extends Specification {
  void "should register bytea postgresql datatype for JDBC's BLOB"() {
    given:
    PostgreSqlAxonDialect postgreSqlAxonDialect = new PostgreSqlAxonDialect()

    expect:
    postgreSqlAxonDialect.getTypeName(Types.BLOB) == "bytea"
  }

  void "should treat JDBC BLOBs as JDBC binary type"() {
    given:
    PostgreSqlAxonDialect postgreSqlAxonDialect = new PostgreSqlAxonDialect()

    when:
    SqlTypeDescriptor sqlTypeDescriptor = postgreSqlAxonDialect.remapSqlTypeDescriptor(BlobTypeDescriptor.BLOB_BINDING)

    then:
    sqlTypeDescriptor == BinaryTypeDescriptor.INSTANCE
    sqlTypeDescriptor.sqlType == Types.BINARY
  }

  void "should not change remapping for other types"() {
    given:
    PostgreSqlAxonDialect postgreSqlAxonDialect = new PostgreSqlAxonDialect()

    when:
    SqlTypeDescriptor sqlTypeDescriptor = postgreSqlAxonDialect.remapSqlTypeDescriptor(VarcharTypeDescriptor.INSTANCE)

    then:
    sqlTypeDescriptor.sqlType == Types.VARCHAR
  }
}
