package net.croz.cargotracker.infrastructure.project.boundary.api.conversation.metadata

import net.croz.cargotracker.infrastructure.project.boundary.api.severity.Severity
import spock.lang.Specification

class ResponseMetaDataReportSpecification extends Specification {
  void "createBasicInfoMetaDataReport - should create expected report"() {
    given:
    ResponseMetaDataReport responseMetaDataReport = ResponseMetaDataReport.createBasicInfoMetaDataReport()

    expect:
    verifyAll {
      responseMetaDataReport.timestamp
      responseMetaDataReport.severity == Severity.INFO
    }
  }
}
