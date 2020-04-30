package net.croz.cargotracker.infrastructure.project.boundary.api.conversation.metadata

import groovy.transform.CompileStatic
import net.croz.cargotracker.infrastructure.project.boundary.api.severity.Severity

import java.time.Instant

@CompileStatic
class ResponseMetaDataReport {
  Instant timestamp
  Severity severity

  String titleText
  String titleDetailedText

  Locale locale

  ResponseMetaDataReportViolationPart violation

  static ResponseMetaDataReport createBasicInfoMetaDataReport() {
    ResponseMetaDataReport responseMetadataReport = new ResponseMetaDataReport(timestamp: Instant.now(), severity: Severity.INFO)
    return responseMetadataReport
  }
}
