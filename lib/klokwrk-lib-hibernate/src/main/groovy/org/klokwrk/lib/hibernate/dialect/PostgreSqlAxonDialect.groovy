package org.klokwrk.lib.hibernate.dialect

import groovy.transform.CompileStatic
import org.hibernate.dialect.PostgreSQL10Dialect
import org.hibernate.type.descriptor.sql.BinaryTypeDescriptor
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor

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
 * For more information, there are few references around the web dealing with this problem:
 * <ul>
 *   <li>https://blog.trifork.com/2017/10/09/axon-postgresql-without-toast/</li>
 *   <li>https://groups.google.com/forum/#!msg/axonframework/HhzQMbWfHTg/G04WbiixBAAJ</li>
 *   <li>https://github.com/AxonIQ/reference-guide/issues/115</li>
 * </ul>
 */
@SuppressWarnings("unused")
@CompileStatic
class PostgreSqlAxonDialect extends PostgreSQL10Dialect {
  PostgreSqlAxonDialect() {
    super()
    registerColumnType(Types.BLOB, "bytea")
  }

  /**
   * Together with registration of BLOB column type in the constructor, causes PostgreSQL to use inline BLOBS ('bytea' type).
   */
  @Override
  SqlTypeDescriptor remapSqlTypeDescriptor(SqlTypeDescriptor sqlTypeDescriptor) {
    if (sqlTypeDescriptor.sqlType == Types.BLOB) {
      return BinaryTypeDescriptor.INSTANCE
    }

    return super.remapSqlTypeDescriptor(sqlTypeDescriptor)
  }
}
