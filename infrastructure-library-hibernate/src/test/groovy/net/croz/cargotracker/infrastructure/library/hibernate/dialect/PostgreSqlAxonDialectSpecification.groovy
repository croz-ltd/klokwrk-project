package net.croz.cargotracker.infrastructure.library.hibernate.dialect

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
