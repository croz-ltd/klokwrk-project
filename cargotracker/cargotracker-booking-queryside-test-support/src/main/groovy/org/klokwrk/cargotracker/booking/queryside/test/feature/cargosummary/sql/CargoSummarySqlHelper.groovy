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
