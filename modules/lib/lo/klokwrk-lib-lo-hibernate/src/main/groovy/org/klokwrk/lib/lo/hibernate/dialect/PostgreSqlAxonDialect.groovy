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
package org.klokwrk.lib.lo.hibernate.dialect

import groovy.transform.CompileStatic
import org.hibernate.boot.model.TypeContributions
import org.hibernate.dialect.DatabaseVersion
import org.hibernate.dialect.PostgreSQLDialect
import org.hibernate.service.ServiceRegistry
import org.hibernate.type.SqlTypes
import org.hibernate.type.descriptor.jdbc.BinaryJdbcType
import org.hibernate.type.descriptor.jdbc.spi.JdbcTypeRegistry

import java.sql.Types

/**
 * Customizes originally provided PostgreSQL dialect to align it with the Axon's EventStore, and especially Axon's TokenStore, usage needs.
 * <p/>
 * In particular customizations are:
 * <ul>
 *   <li>inlined blob handling</li>
 * </ul>
 * <p/>
 * <h3>Inlined blob handling</h3>
 * By default, Hibernate PostgreSql dialect uses 'oid' data type for storing BLOB's. 'oid' data type is suitable for storing very large binary objects (MB or GB) in the space which is outside of
 * user tables. Contrary, inlined blobs ('bytea' data type) should be better performing for small blobs (KB), and are stored directly in columns of user tables.
 * <p/>
 * In general, events stored in Axon event store should not be very big. At worst they will be few hundred KBs. Even when some of events are few MBs in size, 'bytea' should be good enough for storing
 * and using them.
 * <p/>
 * If you are using Axon Server, than only the size of tracking token's BLOBs are concern (events are stored in Axon Server file-system based database). Since tracking tokens are very small (bellow
 * 1 KB), the usage of 'bytea' data type is even more appropriate. Even Axon is using 'bytea' data type in its own implementations of JDBC stores.
 * <p/>
 * This dialect enforces the usage of PostgreSql's 'bytea' data type for storing BLOBs.
 * <p/>
 * For more information, there are few references around the web dealing with this problem. The first reference is probably the most relevant and up-to-date.
 * <ul>
 *   <li>https://developer.axoniq.io/w/axonframework-and-postgresql-without-toast</li>
 *   <li>https://blog.trifork.com/2017/10/09/axon-postgresql-without-toast/</li>
 *   <li>https://groups.google.com/forum/#!msg/axonframework/HhzQMbWfHTg/G04WbiixBAAJ</li>
 *   <li>https://github.com/AxonIQ/reference-guide/issues/115</li>
 * </ul>
 */
@SuppressWarnings("unused")
@CompileStatic
class PostgreSqlAxonDialect extends PostgreSQLDialect {
  static final String BYTEA = "bytea"

  PostgreSqlAxonDialect() {
    super(DatabaseVersion.make(15, 0))
  }

  @Override
  protected String columnType(int sqlTypeCode) {
    if (sqlTypeCode == SqlTypes.BLOB) {
      return BYTEA
    }
    return super.columnType(sqlTypeCode)
  }

  @Override
  protected String castType(int sqlTypeCode) {
    if (sqlTypeCode == SqlTypes.BLOB) {
      return BYTEA
    }
    return super.castType(sqlTypeCode)
  }

  @Override
  void contributeTypes(TypeContributions typeContributions, ServiceRegistry serviceRegistry) {
    super.contributeTypes(typeContributions, serviceRegistry)
    JdbcTypeRegistry jdbcTypeRegistry = typeContributions.typeConfiguration.jdbcTypeRegistry
    jdbcTypeRegistry.addDescriptor(Types.BLOB, BinaryJdbcType.INSTANCE)
  }
}
