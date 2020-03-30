package net.croz.cargotracker.infrastructure.shared.hibernate.dialect

import org.hibernate.dialect.PostgreSQL10Dialect
import org.hibernate.type.descriptor.sql.BinaryTypeDescriptor
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor

import java.sql.Types

/**
 * Customizes originally provided PostgreSQL dialect to align it with the Axon Event Store usage needs.
 * <p/>
 * In particular customizations are:
 * <ul>
 *   <li>native sequence per table usage</li>
 *   <li>inlined blob handling</li>
 * </ul>
 */
@SuppressWarnings("unused")
class PostgreSqlCustomDialect extends PostgreSQL10Dialect {
  PostgreSqlCustomDialect() {
    registerColumnType(Types.BLOB, "bytea")
  }

  /**
   * Together with registration of BLOB column type in constructor, causes PostgreSQL to use inline BLOBS (native postgre 'bytea' type) in tables instead of externalized ones (native postgre 'oid'
   * type).
   * <p/>
   * Inline blobs should be better performing for small blobs (KB) while externalized ones are better for very large blobs (MB or GB). In our case (Axon Token store) blobs are bellow 1 KB so inline
   * blobs are preferred.
   * <p/>
   * Usage of 'bytea' postgre type is consistent with types that Axon uses in theirs JDBC stores.
   */
  @Override
  SqlTypeDescriptor remapSqlTypeDescriptor(SqlTypeDescriptor sqlTypeDescriptor) {
    if (sqlTypeDescriptor.getSqlType() == Types.BLOB) {
      return BinaryTypeDescriptor.INSTANCE
    }

    return super.remapSqlTypeDescriptor(sqlTypeDescriptor)
  }

  /**
   * Use sequence per table instead of a single 'hibernate_sequence' sequence.
   * <p/>
   * By default postgresql dialect uses a single sequence "hibernate_sequence" for all identifiers. This strategy causes the problems with projections with multiple applied events where applying
   * previous events is not reflected and flushed in database before next event handling is started. Therefore this dialect uses identity strategy which in postgresql creates a sequence per table and
   * projections work as expected after that.
   */
  @Override
  String getNativeIdentifierGeneratorStrategy() {
    return "identity"
  }
}
