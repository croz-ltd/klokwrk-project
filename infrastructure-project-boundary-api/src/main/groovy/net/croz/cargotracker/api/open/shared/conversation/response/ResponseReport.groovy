package net.croz.cargotracker.api.open.shared.conversation.response

import groovy.transform.CompileStatic
import net.croz.cargotracker.api.open.shared.exceptional.violation.Severity

import java.time.Instant

@CompileStatic
class ResponseReport {
  Instant timestamp
  Severity severity

  String titleText
  String titleDetailedText

  Locale locale

  ResponseReportViolationPart violation

  static ResponseReport createBasicInfoReport() {
    ResponseReport responseReport = new ResponseReport(
        timestamp: Instant.now(),
        severity: Severity.INFO
    )

    return responseReport
  }
}
