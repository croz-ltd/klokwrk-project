package org.klokwrk.cargotracker.lib.web.metadata.report

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.lib.boundary.api.metadata.report.ResponseMetaDataReport

@CompileStatic
class HttpResponseMetaDataReport extends ResponseMetaDataReport {
  HttpResponseMetaDataReportPart http
}
